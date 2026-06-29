#!/usr/bin/env python3
import json
import math
import subprocess
import sys
import urllib.parse
import urllib.request
from datetime import date, timedelta

DB_NAME = "quant_invest_data"
MYSQL_USER = "root"
MYSQL_PASSWORD = "12345678"

STOCKS = {
    "000001": "平安银行",
    "000002": "万科A",
    "000063": "中兴通讯",
    "000333": "美的集团",
    "000651": "格力电器",
    "000858": "五粮液",
    "002415": "海康威视",
    "300750": "宁德时代",
    "600036": "招商银行",
    "600519": "贵州茅台",
}

BENCHMARKS = {
    "000300": "沪深300",
}


def sina_symbol(code):
    if code in {"000300", "000016"}:
        return "sh" + code
    if code.startswith(("6", "9")):
        return "sh" + code
    if code.startswith("8"):
        return "bj" + code
    return "sz" + code


def fetch_daily(code):
    symbol = urllib.parse.quote(sina_symbol(code))
    url = (
        "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/"
        f"CN_MarketData.getKLineData?symbol={symbol}&scale=240&ma=no&datalen=430"
    )
    with urllib.request.urlopen(url, timeout=15) as response:
        body = response.read().decode("utf-8")
    rows = json.loads(body)
    parsed = {}
    for row in rows:
        parsed[date.fromisoformat(row["day"])] = {
            "open": float(row["open"]),
            "high": float(row["high"]),
            "low": float(row["low"]),
            "close": float(row["close"]),
            "volume": int(float(row["volume"])),
        }
    return parsed


def fill_last_year(raw_rows):
    end = date.today()
    start = end - timedelta(days=364)
    days = [start + timedelta(days=i) for i in range((end - start).days + 1)]
    available = sorted(raw_rows)
    first_before = None
    for day in available:
        if day <= start:
            first_before = raw_rows[day]
        else:
            break
    previous = first_before or raw_rows[available[0]]
    filled = []
    for day in days:
        if day in raw_rows:
            previous = raw_rows[day]
        filled.append({"date": day, **previous})
    return filled


def dec(value):
    return f"{value:.4f}"


def moving_average(rows, index, period):
    if index + 1 < period:
        return None
    return sum(row["close"] for row in rows[index - period + 1:index + 1]) / period


def ema(value, previous, period):
    return value * 2 / (period + 1) + previous * (period - 1) / (period + 1)


def calculate_indicators(code, rows):
    indicators = []
    ema12 = 0
    ema26 = 0
    dea = 0
    avg_gain = 0
    avg_loss = 0
    for index, row in enumerate(rows):
        close = row["close"]
        if index == 0:
            ema12 = close
            ema26 = close
        else:
            ema12 = ema(close, ema12, 12)
            ema26 = ema(close, ema26, 26)
        dif = ema12 - ema26
        dea = dif if index == 0 else ema(dif, dea, 9)
        macd = (dif - dea) * 2
        rsi = None
        if index > 0:
            change = close - rows[index - 1]["close"]
            gain = max(change, 0)
            loss = max(-change, 0)
            if index <= 14:
                avg_gain += gain
                avg_loss += loss
                if index == 14:
                    avg_gain /= 14
                    avg_loss /= 14
                    rsi = 100 if avg_loss == 0 else 100 - 100 / (1 + avg_gain / avg_loss)
            else:
                avg_gain = (avg_gain * 13 + gain) / 14
                avg_loss = (avg_loss * 13 + loss) / 14
                rsi = 100 if avg_loss == 0 else 100 - 100 / (1 + avg_gain / avg_loss)
        indicators.append({
            "code": code,
            "date": row["date"],
            "ma5": moving_average(rows, index, 5),
            "ma10": moving_average(rows, index, 10),
            "ma20": moving_average(rows, index, 20),
            "dif": dif,
            "dea": dea,
            "macd": macd,
            "rsi": rsi,
        })
    return indicators


def sql_value(value):
    if value is None or (isinstance(value, float) and math.isnan(value)):
        return "NULL"
    if isinstance(value, date):
        return f"'{value.isoformat()}'"
    if isinstance(value, str):
        return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"
    if isinstance(value, float):
        return dec(value)
    return str(value)


def build_sql(all_rows, all_indicators):
    sql = [
        f"CREATE DATABASE IF NOT EXISTS {DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
        f"USE {DB_NAME};",
        """
CREATE TABLE IF NOT EXISTS stock_kline (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(16) NOT NULL,
  date DATE NOT NULL,
  open DECIMAL(12, 4) NOT NULL,
  high DECIMAL(12, 4) NOT NULL,
  low DECIMAL(12, 4) NOT NULL,
  close DECIMAL(12, 4) NOT NULL,
  volume BIGINT NOT NULL,
  UNIQUE KEY uk_stock_kline_code_date (code, date),
  INDEX idx_stock_kline_code_date (code, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""",
        """
CREATE TABLE IF NOT EXISTS stock_indicator (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(16) NOT NULL,
  date DATE NOT NULL,
  ma5 DECIMAL(12, 4),
  ma10 DECIMAL(12, 4),
  ma20 DECIMAL(12, 4),
  dif DECIMAL(12, 4),
  dea DECIMAL(12, 4),
  macd DECIMAL(12, 4),
  rsi DECIMAL(12, 4),
  UNIQUE KEY uk_stock_indicator_code_date (code, date),
  INDEX idx_stock_indicator_code_date (code, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""",
        """
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL UNIQUE,
  display_name VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  password_hash VARCHAR(128) NOT NULL,
  can_view_data TINYINT(1) NOT NULL DEFAULT 1,
  can_manage_users TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""",
        "ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS password_hash VARCHAR(128) NOT NULL DEFAULT '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92' AFTER role;",
        """
CREATE TABLE IF NOT EXISTS user_stock (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  code VARCHAR(16) NOT NULL,
  UNIQUE KEY uk_user_stock_username_code (username, code),
  INDEX idx_user_stock_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""",
        """
CREATE TABLE IF NOT EXISTS trade_strategy (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  trade_date DATE NOT NULL,
  code VARCHAR(16) NOT NULL,
  type VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  INDEX idx_trade_strategy_username_date (username, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
""",
        "DELETE FROM trade_strategy;",
        "DELETE FROM user_stock;",
        "DELETE FROM stock_indicator;",
        "DELETE FROM stock_kline;",
        "DELETE FROM sys_user;",
        "ALTER TABLE trade_strategy AUTO_INCREMENT = 1;",
        "ALTER TABLE user_stock AUTO_INCREMENT = 1;",
        "ALTER TABLE stock_indicator AUTO_INCREMENT = 1;",
        "ALTER TABLE stock_kline AUTO_INCREMENT = 1;",
        "ALTER TABLE sys_user AUTO_INCREMENT = 1;",
        """
INSERT INTO sys_user(username, display_name, role, password_hash, can_view_data, can_manage_users) VALUES
('viewer', '普通用户', 'USER', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1, 0),
('admin', '管理员', 'ADMIN', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1, 1);
""",
    ]
    for code, rows in all_rows.items():
        values = []
        for row in rows:
            values.append(
                "(" + ",".join([
                    sql_value(code),
                    sql_value(row["date"]),
                    sql_value(row["open"]),
                    sql_value(row["high"]),
                    sql_value(row["low"]),
                    sql_value(row["close"]),
                    sql_value(row["volume"]),
                ]) + ")"
            )
        sql.append(
            "INSERT INTO stock_kline(code, date, open, high, low, close, volume) VALUES\n"
            + ",\n".join(values)
            + ";"
        )
    for code, indicators in all_indicators.items():
        values = []
        for row in indicators:
            values.append(
                "(" + ",".join([
                    sql_value(row["code"]),
                    sql_value(row["date"]),
                    sql_value(row["ma5"]),
                    sql_value(row["ma10"]),
                    sql_value(row["ma20"]),
                    sql_value(row["dif"]),
                    sql_value(row["dea"]),
                    sql_value(row["macd"]),
                    sql_value(row["rsi"]),
                ]) + ")"
            )
        sql.append(
            "INSERT INTO stock_indicator(code, date, ma5, ma10, ma20, dif, dea, macd, rsi) VALUES\n"
            + ",\n".join(values)
            + ";"
        )
    user_stock_rows = []
    for username in ("viewer", "admin"):
        for code in STOCKS:
            user_stock_rows.append(f"('{username}', '{code}')")
    sql.append("INSERT INTO user_stock(username, code) VALUES\n" + ",\n".join(user_stock_rows) + ";")
    return "\n".join(sql)


def main():
    codes = {**STOCKS, **BENCHMARKS}
    all_rows = {}
    all_indicators = {}
    for code, name in codes.items():
        print(f"Fetching {code} {name} ...", file=sys.stderr)
        raw = fetch_daily(code)
        if not raw:
            raise RuntimeError(f"No data fetched for {code}")
        rows = fill_last_year(raw)
        all_rows[code] = rows
        all_indicators[code] = calculate_indicators(code, rows)

    sql = build_sql(all_rows, all_indicators)
    command = [
        "mysql",
        f"-u{MYSQL_USER}",
        f"-p{MYSQL_PASSWORD}",
        "--default-character-set=utf8mb4",
    ]
    subprocess.run(command, input=sql, text=True, check=True)
    print(f"Inserted {len(STOCKS)} stocks plus {len(BENCHMARKS)} benchmark, {len(next(iter(all_rows.values())))} days each.")


if __name__ == "__main__":
    main()

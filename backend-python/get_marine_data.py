import requests
import pandas as pd
from datetime import datetime, timedelta

# Lokasi (ubah ke lokasi laut yang kamu mau)
LAT, LON = -9.0, 110.0   # Laut Selatan Jawa (ada data gelombang)

# Rentang waktu
end_date = datetime.utcnow().date()
start_date = end_date - timedelta(days=30)

# --- 1️⃣ Ambil data laut (gelombang) ---
marine_url = (
    f"https://marine-api.open-meteo.com/v1/marine?"
    f"latitude={LAT}&longitude={LON}"
    f"&start_date={start_date}&end_date={end_date}"
    f"&hourly=wave_height,wave_direction,wave_period"
    f"&timezone=Asia%2FJakarta"
)
marine_res = requests.get(marine_url).json()
marine_df = pd.DataFrame({
    "timestamp": marine_res["hourly"]["time"],
    "wave_height_m": marine_res["hourly"]["wave_height"],
    "wave_dir_deg": marine_res["hourly"]["wave_direction"],
    "wave_period_s": marine_res["hourly"]["wave_period"]
})
marine_df["timestamp"] = pd.to_datetime(marine_df["timestamp"])

# --- 2️⃣ Ambil data cuaca (angin, suhu, tekanan, hujan) ---
weather_url = (
    f"https://api.open-meteo.com/v1/forecast?"
    f"latitude={LAT}&longitude={LON}"
    f"&start_date={start_date}&end_date={end_date}"
    f"&hourly=temperature_2m,pressure_msl,relative_humidity_2m,"
    f"wind_speed_10m,wind_direction_10m,precipitation"
    f"&timezone=Asia%2FJakarta"
)
weather_res = requests.get(weather_url).json()
weather_df = pd.DataFrame({
    "timestamp": weather_res["hourly"]["time"],
    "temp_c": weather_res["hourly"]["temperature_2m"],
    "pressure_hpa": weather_res["hourly"]["pressure_msl"],
    "humidity_pct": weather_res["hourly"]["relative_humidity_2m"],
    "wind_speed_mps": weather_res["hourly"]["wind_speed_10m"],
    "wind_dir_deg_weather": weather_res["hourly"]["wind_direction_10m"],
    "precip_mm": weather_res["hourly"]["precipitation"]
})
weather_df["timestamp"] = pd.to_datetime(weather_df["timestamp"])

# --- 3️⃣ Gabungkan dua data berdasarkan timestamp ---
df = pd.merge_asof(
    marine_df.sort_values("timestamp"),
    weather_df.sort_values("timestamp"),
    on="timestamp",
    direction="nearest"
)

# --- 4️⃣ Bersihkan & susun kolom ---
df = df.rename(columns={"wind_dir_deg_weather": "wind_dir_deg"})
df = df[[
    "timestamp", "wave_height_m", "wave_period_s", "wave_dir_deg",
    "wind_speed_mps", "temp_c", "pressure_hpa", "humidity_pct", "precip_mm"
]]
df = df.dropna().reset_index(drop=True)

# --- 5️⃣ Simpan ke file ---
output_file = "sea_weather_dataset.csv"
df.to_csv(output_file, index=False)

print(f"✅ Dataset cuaca laut berhasil dibuat: {output_file}")
print(df.head(10))

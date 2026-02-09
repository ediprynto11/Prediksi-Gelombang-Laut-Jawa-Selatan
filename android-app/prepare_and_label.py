# prepare_and_label.py
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler
import joblib

CSV = "sea_weather_dataset.csv"
TIMESTEPS = 48   # gunakan 24 jam terakhir sebagai input
FORECAST_HOURS = 72  # prediksi 72 jam ke depan

def wind_dir_to_sincos(df, col='wind_dir_deg'):
    df[col] = df[col].fillna(method='ffill')
    df['wind_dir_sin'] = np.sin(np.deg2rad(df[col].fillna(0)))
    df['wind_dir_cos'] = np.cos(np.deg2rad(df[col].fillna(0)))
    return df

def make_safety_label(wave_h, wind_spd):
    # 0 = Aman, 1 = Waspada, 2 = Bahaya
    if (wave_h >= 2.5) or (wind_spd >= 12.0):
        return 2
    if (wave_h >= 1.0) or (wind_spd >= 7.0):
        return 1
    return 0

def load_and_prepare(csv=CSV):
    df = pd.read_csv(csv, parse_dates=['timestamp'])
    df = df.sort_values('timestamp').set_index('timestamp').resample('1H').mean()
    # simple interpolate
    df = df.interpolate(limit_direction='both')

    # create sin/cos for wind dir (use wave_dir if wind_dir missing)
    if 'wind_dir_deg' not in df.columns or df['wind_dir_deg'].isna().all():
        # fallback to wave_dir_deg if exists
        if 'wave_dir_deg' in df.columns:
            df['wind_dir_deg'] = df['wave_dir_deg']
        else:
            df['wind_dir_deg'] = 0.0
    df = wind_dir_to_sincos(df, 'wind_dir_deg')

    # features for model
    features = ['wave_height_m', 'wave_period_s', 'wind_speed_mps',
                'wind_dir_sin', 'wind_dir_cos', 'temp_c', 'pressure_hpa', 'precip_mm']
    # Ensure columns exist
    for f in features:
        if f not in df.columns:
            df[f] = 0.0

    # scale features
    scaler = StandardScaler()
    values = df[features].values
    scaled = scaler.fit_transform(values)
    joblib.dump(scaler, "scaler.pkl")
    print("Scaler saved -> scaler.pkl")

    # Create sequences (X) and multi-step targets (Y)
    Xs, Ys = [], []
    safety_labels = []
    for i in range(TIMESTEPS, len(df) - FORECAST_HOURS + 1):
        x = scaled[i-TIMESTEPS:i]  # shape (TIMESTEPS, n_features)
        # target wave_height for next FORECAST_HOURS hours (we predict wave_height and wind_speed and precip maybe)
        future = df.iloc[i:i+FORECAST_HOURS]
        # Regression targets: multi-step for wave_height and wind_speed and precip
        y_reg = future[['wave_height_m','wind_speed_mps','precip_mm']].values  # shape (FORECAST_HOURS, 3)
        # For classification label we can use worst-case in next 24/72h:
        # here we label based on maximum condition in next 24 hours (today safety)
        next24 = df.iloc[i:i+24]
        max_wave_24 = next24['wave_height_m'].max()
        max_wind_24 = next24['wind_speed_mps'].max()
        label_today = make_safety_label(max_wave_24, max_wind_24)
        # also label for next72 (worst in 72h)
        max_wave_72 = y_reg[:,0].max()
        max_wind_72 = y_reg[:,1].max()
        label_72 = make_safety_label(max_wave_72, max_wind_72)

        Xs.append(x)
        Ys.append(y_reg)
        # We'll store both today and 72h labels; combine as dict-like arrays
        safety_labels.append([label_today, label_72])

    Xs = np.array(Xs)           # (N, TIMESTEPS, n_features)
    Ys = np.array(Ys)           # (N, FORECAST_HOURS, 3)
    safety_labels = np.array(safety_labels)  # (N, 2)

    np.savez_compressed("prepared_data.npz", X=Xs, Y=Ys, safety=safety_labels)
    print("Saved prepared_data.npz -> shapes:", Xs.shape, Ys.shape, safety_labels.shape)
    return Xs, Ys, safety_labels

if __name__ == "__main__":
    load_and_prepare()

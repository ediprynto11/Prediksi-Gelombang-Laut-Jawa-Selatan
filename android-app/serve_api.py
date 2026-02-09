# # serve_api.py
# from flask import Flask, request, jsonify
# import numpy as np
# import joblib
# from tensorflow.keras.models import load_model
# import traceback

# app = Flask(__name__)

# # === Load models ===
# LSTM_MODEL_PATH = "lstm_seq2seq.keras"
# SAFETY_TODAY_PATH = "safety_clf_today.pkl"
# SAFETY_72_PATH = "safety_clf_72.pkl"

# print("🔹 Loading models ...")
# lstm_model = load_model(LSTM_MODEL_PATH)
# clf_today = joblib.load(SAFETY_TODAY_PATH)
# clf_72 = joblib.load(SAFETY_72_PATH)
# print("✅ Models loaded successfully")

# # === Utility: make features for safety classifier ===
# def make_feat_set(Xarr):
#     last = Xarr[:, -1, :]   # last timestep
#     mean = Xarr.mean(axis=1)
#     std = Xarr.std(axis=1)
#     feat = np.concatenate([last, mean, std], axis=1)
#     return feat


# @app.route("/predict", methods=["POST"])
# def predict():
#     try:
#         data = request.get_json()
#         if not data or "sequence" not in data:
#             return jsonify({"error": "Missing 'sequence' data"}), 400

#         seq = np.array(data["sequence"], dtype=float)  # shape (timesteps, n_features)
#         if seq.ndim != 2:
#             return jsonify({"error": "Input must be 2D array (timesteps x features)"}), 400

#         seq = np.expand_dims(seq, axis=0)  # (1, timesteps, features)

#         # Buat decoder input kosong
#         out_steps = 72
#         out_features = 3  # misal: wave, wind, rain
#         decoder_input = np.zeros((1, out_steps, out_features))

#         # Prediksi cuaca 72 jam ke depan
#         forecast = lstm_model.predict([seq, decoder_input])[0].tolist()

#         # Prediksi keselamatan
#         feat = make_feat_set(seq)
#         safe_today = int(clf_today.predict(feat)[0])
#         safe_72 = int(clf_72.predict(feat)[0])

#         result = {
#             "forecast_3day": forecast,
#             "safety": {
#                 "today": "Aman" if safe_today == 1 else "Bahaya",
#                 "72h": "Aman" if safe_72 == 1 else "Bahaya"
#             }
#         }
#         return jsonify(result)

#     except Exception as e:
#         traceback.print_exc()
#         return jsonify({"error": str(e)}), 500


# @app.route("/", methods=["GET"])
# def home():
#     return jsonify({"message": "API Cuaca Laut Jawa aktif ✅"})


# if __name__ == "__main__":
#     app.run(host="0.0.0.0", port=5050, debug=True)

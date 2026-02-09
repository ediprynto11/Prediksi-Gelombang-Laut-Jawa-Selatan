import numpy as np
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, LSTM, Dense, TimeDistributed
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
import os

DATA = "prepared_data.npz"
SCALER = "scaler.pkl"
LSTM_MODEL = "lstm_seq2seq.h5"
SAFETY_CLF = "safety_clf.pkl"

def build_seq2seq(timesteps=48, n_features=8, out_steps=72, out_features=3):
    # Encoder
    encoder_inputs = Input(shape=(timesteps, n_features))
    encoder = LSTM(128, return_state=True)
    encoder_outputs, state_h, state_c = encoder(encoder_inputs)
    encoder_states = [state_h, state_c]

    # Decoder
    decoder_inputs = Input(shape=(out_steps, out_features))
    decoder_lstm = LSTM(128, return_sequences=True, return_state=True)
    dec_outputs, _, _ = decoder_lstm(decoder_inputs, initial_state=encoder_states)
    decoder_dense = TimeDistributed(Dense(out_features))
    decoder_outputs = decoder_dense(dec_outputs)

    model = Model([encoder_inputs, decoder_inputs], decoder_outputs)
    model.compile(optimizer='adam', loss='mse', metrics=['mae'])
    return model

def train():
    d = np.load(DATA)
    X = d['X']
    Y = d['Y']
    safety = d['safety']

    N, TIMESTEPS, N_FEATURES = X.shape
    OUT_STEPS, OUT_FEATURES = Y.shape[1], Y.shape[2]

    split = int(N * 0.8)
    X_train, X_val = X[:split], X[split:]
    Y_train, Y_val = Y[:split], Y[split:]
    safety_train, safety_val = safety[:split], safety[split:]

    model = build_seq2seq(TIMESTEPS, N_FEATURES, OUT_STEPS, OUT_FEATURES)
    model.summary()

    decoder_input_train = np.zeros_like(Y_train)
    decoder_input_val = np.zeros_like(Y_val)

    es = EarlyStopping(monitor='val_loss', patience=8, restore_best_weights=True)
    ckpt = ModelCheckpoint(LSTM_MODEL, save_best_only=True, monitor='val_loss', verbose=1)
    model.fit([X_train, decoder_input_train], Y_train,
              validation_data=([X_val, decoder_input_val], Y_val),
              epochs=80, batch_size=32, callbacks=[es, ckpt])

    print("Saved LSTM model ->", LSTM_MODEL)

    # ✅ Tambahan baru: simpan dalam format Keras modern (.keras)
    model.save("lstm_seq2seq.keras")
    print("Saved new Keras model -> lstm_seq2seq.keras")

    # === Safety classifier ===
    def make_feat_set(Xarr):
        last = Xarr[:, -1, :]
        mean = Xarr.mean(axis=1)
        std = Xarr.std(axis=1)
        feat = np.concatenate([last, mean, std], axis=1)
        return feat

    feat_train = make_feat_set(X_train)
    feat_val = make_feat_set(X_val)

    clf_today = RandomForestClassifier(n_estimators=200, random_state=42)
    clf_today.fit(feat_train, safety_train[:, 0])
    pred_today = clf_today.predict(feat_val)
    print("Classification report — Today:")
    print(classification_report(safety_val[:, 0], pred_today))

    clf_72 = RandomForestClassifier(n_estimators=200, random_state=42)
    clf_72.fit(feat_train, safety_train[:, 1])
    pred_72 = clf_72.predict(feat_val)
    print("Classification report — 72h:")
    print(classification_report(safety_val[:, 1], pred_72))

    joblib.dump(clf_today, "safety_clf_today.pkl")
    joblib.dump(clf_72, "safety_clf_72.pkl")
    print("Saved safety classifiers: safety_clf_today.pkl, safety_clf_72.pkl")

if __name__ == "__main__":
    train()

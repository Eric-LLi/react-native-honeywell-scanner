package nl.volst.HoneywellScanner;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import static nl.volst.HoneywellScanner.HoneywellScannerPackage.TAG;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.UnsupportedPropertyException;

@SuppressWarnings("unused")
public class HoneywellScannerModule extends ReactContextBaseJavaModule implements BarcodeReader.BarcodeListener, LifecycleEventListener {

	// Debugging
	private static final boolean D = true;

	private static BarcodeReader barcodeReader;
	private AidcManager manager;
	private BarcodeReader reader;
	private ReactApplicationContext mReactContext;

	private static final String BARCODE_READ_SUCCESS = "barcodeReadSuccess";
	private static final String BARCODE_READ_FAIL = "barcodeReadFail";

	public HoneywellScannerModule(ReactApplicationContext reactContext) {
		super(reactContext);
		mReactContext = reactContext;
		mReactContext.addLifecycleEventListener(this);
	}

	@Override
	public String getName() {
		return "HoneywellScanner";
	}


	@Override
	public void onHostResume() {
		try {
			if (reader != null) {
				reader.claim();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onHostPause() {
		try {
			if (reader != null) {
				// release the scanner claim so we don't get any scanner
				// notifications while paused.
				reader.release();
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	@Override
	public void onHostDestroy() {
		try {
			if (reader != null) {
				reader.removeBarcodeListener(this);
				reader.close();

				reader = null;
			}

			if (manager != null) {
				manager.close();

				manager = null;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/**
	 * Send event to javascript
	 *
	 * @param eventName Name of the event
	 * @param params    Additional params
	 */
	private void sendEvent(String eventName, @Nullable WritableMap params) {
		if (mReactContext.hasActiveCatalystInstance()) {
			if (D) Log.d(TAG, "Sending event: " + eventName);
			mReactContext
					.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
					.emit(eventName, params);
		}
	}

	public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
		if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan read");
		WritableMap params = Arguments.createMap();
		params.putString("data", barcodeReadEvent.getBarcodeData());
		sendEvent(BARCODE_READ_SUCCESS, params);
	}

	public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
		if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan failed");
		sendEvent(BARCODE_READ_FAIL, null);
	}

	/*******************************/
	/** Methods Available from JS **/
	/*******************************/

	@ReactMethod
	public void startReader(final Promise promise) {
		AidcManager.create(mReactContext, new CreatedCallback() {
			@Override
			public void onCreated(AidcManager aidcManager) {
				manager = aidcManager;

				if (reader != null) {
					reader.release();
					reader.close();
				}

				try {
					reader = manager.createBarcodeReader("dcs.scanner.ring");

					reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
							BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);

					reader.setProperty(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
					reader.setProperty(BarcodeReader.PROPERTY_UPC_A_CHECK_DIGIT_TRANSMIT_ENABLED, true);
					reader.setProperty(BarcodeReader.PROPERTY_UPC_E_CHECK_DIGIT_TRANSMIT_ENABLED, true);
					reader.setProperty(BarcodeReader.PROPERTY_EAN_8_CHECK_DIGIT_TRANSMIT_ENABLED, true);
					reader.setProperty(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);

					// Enable bad read response
					reader.setProperty(BarcodeReader.PROPERTY_CENTER_DECODE, true);

					// Turn on center decoding
					reader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);

					//Enable Datamatrix
					reader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);

					// register bar code event listener
					reader.addBarcodeListener(HoneywellScannerModule.this);

					reader.claim();

					promise.resolve(true);
				} catch (ScannerUnavailableException e) {
					promise.reject(e);
				} catch (UnsupportedPropertyException e) {
					promise.reject(e);
				} catch (Exception e) {
					promise.reject(e);
				}
			}
		});
	}

	@ReactMethod
	public void stopReader(Promise promise) {
		try {
			if (reader != null) {
				reader.close();

				reader = null;
			}
			if (manager != null) {
				manager.close();

				manager = null;
			}
			promise.resolve(null);
		} catch (Exception ex) {
			promise.reject(ex);
		}

	}

	@ReactMethod
	public void StartScan(Promise promise) {
		if (reader != null) {
			try {
//				reader.softwareTrigger(true);

				reader.aim(true);
				reader.light(true);
				reader.decode(true);

				promise.resolve(true);
			} catch (ScannerNotClaimedException e) {
				// TODO Auto-generated catch block
				promise.reject(e);
			} catch (ScannerUnavailableException e) {
				// TODO Auto-generated catch block
				promise.reject(e);
			}
		}
	}

	@ReactMethod
	public void StopScan(Promise promise) {
		if (reader != null) {
			try {
//				reader.softwareTrigger(false);

				reader.aim(false);
				reader.light(false);
				reader.decode(false);

				promise.resolve(true);
			} catch (ScannerNotClaimedException e) {
				// TODO Auto-generated catch block
				promise.reject(e);
			} catch (ScannerUnavailableException e) {
				// TODO Auto-generated catch block
				promise.reject(e);
			}
		}
	}

	private boolean isCompatible() {
		// This... is not optimal. Need to find a better way to performantly check whether device has a Honeywell scanner
		return Build.BRAND.toLowerCase().contains("honeywell");
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		constants.put("BARCODE_READ_SUCCESS", BARCODE_READ_SUCCESS);
		constants.put("BARCODE_READ_FAIL", BARCODE_READ_FAIL);
		constants.put("isCompatible", isCompatible());
		return constants;
	}

}

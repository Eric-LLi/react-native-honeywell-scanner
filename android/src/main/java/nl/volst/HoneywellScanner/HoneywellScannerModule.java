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

public class HoneywellScannerModule extends ReactContextBaseJavaModule implements BarcodeReader.BarcodeListener, LifecycleEventListener {

    // Debugging
    private static final boolean D = true;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private static BarcodeReader reader;
    private static ReactApplicationContext mReactContext;
    private static boolean isClaimed = false;

    private static boolean isReading = false;

    private static final String BARCODE_STATUS = "BARCODE_STATUS";
    private static final String BARCODE_READ_SUCCESS = "barcodeReadSuccess";
    private static final String BARCODE_READ_FAIL = "barcodeReadFail";
    private static final String BARCODE = "BARCODE";

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
//		try {
//			if (reader != null) {
//				if (!isClaimed) {
//					reader.claim();
//
//					isClaimed = true;
//				}
//			}
//		} catch (Exception ex) {
//			Log.e(TAG, ex.getMessage());
//		}
    }

    @Override
    public void onHostPause() {
//		try {
//			if (reader != null) {
//				doStopScan();
//
//				// release the scanner claim so we don't get any scanner
//				// notifications while paused.
//				if (isClaimed) {
//					reader.release();
//
//					isClaimed = false;
//				}
//			}
//		} catch (Exception ex) {
//			Log.e(TAG, ex.getMessage());
//		}
    }

    @Override
    public void onHostDestroy() {
        try {
            if (isReading) {
                doStopScan();
            }

            if (isClaimed) {
                doReleaseScanner();
            }

            doClose();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
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

    private void sendEvent(String eventName, @Nullable String params) {
        if (mReactContext.hasActiveCatalystInstance()) {
            if (D) Log.d(TAG, "Sending event: " + eventName);
            mReactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan read");
//        WritableMap params = Arguments.createMap();
//        params.putString("data", barcodeReadEvent.getBarcodeData());
//        sendEvent(BARCODE_READ_SUCCESS, params);
        sendEvent(BARCODE, barcodeReadEvent.getBarcodeData());
    }

    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan failed");
//        sendEvent(BARCODE_READ_FAIL, null);
    }

    /*******************************/
    /** Methods Available from JS **/
    /*******************************/

    @ReactMethod
    public void startReader(final boolean isExternal, final Promise promise) {
        Log.d(TAG, "startReader");

        try {
            if (isReading) {
                doStopScan();
            }

            if (isClaimed) {
                doReleaseScanner();
            }

            doClose();

            AidcManager.create(mReactContext, new CreatedCallback() {
                @Override
                public void onCreated(AidcManager aidcManager) {

                    manager = aidcManager;

                    try {
                        if (isExternal) {
                            reader = manager.createBarcodeReader("dcs.scanner.ring");
                            reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                    BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                        } else {
                            reader = manager.createBarcodeReader();
                            reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        }
                        reader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
                        reader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_GOOD_READ_ENABLED, false);

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

                        doClaimScanner();

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
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void stopReader(Promise promise) {
        Log.d(TAG, "stopReader");

        try {

            if (isReading) {
                doStopScan();
            }

            if (isClaimed) {
                doReleaseScanner();
            }

            doClose();

            promise.resolve(null);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void StartScan(Promise promise) {
        try {
            doStartScan();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void StopScan(Promise promise) {
        try {
            doStopScan();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private void doClose() {
        Log.d(TAG, "doClose");

        if (reader != null) {
            reader.removeBarcodeListener(this);
            reader.close();

            reader = null;
        }

        if (manager != null) {
            manager.close();

            manager = null;
        }
    }

    private void doClaimScanner() throws ScannerUnavailableException {
        Log.d(TAG, "doClaimScanner");

        if (reader != null) {
            if (!isClaimed) {
                reader.claim();

                isClaimed = true;
            }
        }
    }

    private void doReleaseScanner() {
        Log.d(TAG, "doReleaseScanner");

        if (reader != null) {
            if (isClaimed) {
                reader.release();

                isClaimed = false;
            }
        }
    }

    private void doStartScan() throws ScannerUnavailableException, ScannerNotClaimedException, InterruptedException {
        Log.d(TAG, "doStartScan");

        if (reader != null) {
            if (isReading) {
                doStopScan();

                Thread.sleep(500);
            }

            reader.aim(true);
            reader.light(true);
            reader.decode(true);

            isReading = true;

            WritableMap map = Arguments.createMap();
            map.putBoolean("status", true);
            sendEvent(BARCODE_STATUS, map);
        }
    }

    private void doStopScan() throws ScannerUnavailableException, ScannerNotClaimedException {
        Log.d(TAG, "doStopScan");

        if (reader != null && isReading) {
            reader.aim(false);
            reader.light(false);
            reader.decode(false);

            isReading = false;

            WritableMap map = Arguments.createMap();
            map.putBoolean("status", false);
            sendEvent(BARCODE_STATUS, map);
        }
    }
//
//    private boolean isCompatible() {
//        // This... is not optimal. Need to find a better way to performantly check whether device has a Honeywell scanner
//        return Build.BRAND.toLowerCase().contains("honeywell");
//    }
//
//    @Override
//    public Map<String, Object> getConstants() {
//        final Map<String, Object> constants = new HashMap<>();
//        constants.put("BARCODE_STATUS", BARCODE_STATUS);
//        constants.put("BARCODE_READ_SUCCESS", BARCODE_READ_SUCCESS);
//        constants.put("BARCODE_READ_FAIL", BARCODE_READ_FAIL);
//        constants.put("isCompatible", isCompatible());
//        return constants;
//    }

}

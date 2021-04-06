import { NativeModules, NativeEventEmitter } from 'react-native';
const { HoneywellScanner } = NativeModules;

export const SCANNER_EVENTS = {
  STATUS : 'BARCODE_STATUS',
	SUCCESS: 'barcodeReadSuccess',
	FAIL: 'barcodeReadFail',
};

const allowedEvents = [
  HoneywellScanner.BARCODE_STATUS,
  HoneywellScanner.BARCODE_READ_SUCCESS,
  HoneywellScanner.BARCODE_READ_FAIL,
];

const events = {};
const eventEmitter = new NativeEventEmitter(HoneywellScanner);
/**
 * Listen for available events
 * @param  {String} event Name of event one of barcodeReadSuccess, barcodeReadFail
 * @param  {Function} handler Event handler
 */
HoneywellScanner.on = (event, handler) => {
  const eventListener = eventEmitter.addListener(event, handler);

	events[event] =  events[event] ? [...events[event], eventListener]: [eventListener];
};

/**
 * Stop listening for event
 * @param  {String} event Name of event one of barcodeReadSuccess, barcodeReadFail
 */
HoneywellScanner.off = (event) => {
  if (Object.hasOwnProperty.call(events, event)) {
		const eventListener = events[event].shift();

		if(eventListener) eventListener.remove();
	}
};

export default HoneywellScanner;

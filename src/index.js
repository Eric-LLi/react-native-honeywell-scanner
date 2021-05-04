import { NativeModules, NativeEventEmitter } from 'react-native';

const { HoneywellScanner } = NativeModules;

const events = {};

const eventEmitter = new NativeEventEmitter(HoneywellScanner);

HoneywellScanner.on = (event, handler) => {
	const eventListener = eventEmitter.addListener(event, handler);

	events[event] =  events[event] ? [...events[event], eventListener]: [eventListener];
};

HoneywellScanner.off = (event) => {
	if (Object.hasOwnProperty.call(events, event)) {
		const eventListener = events[event].shift();

		if(eventListener) eventListener.remove();
	}
};

export default HoneywellScanner;

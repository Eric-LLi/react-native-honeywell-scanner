export declare function startReader(): Promise<boolean>;
export declare function stopReader(): Promise<null>;

export declare function StartScan(): Promise<boolean>;
export declare function StopScan(): Promise<boolean>;

export declare function on(val: SCANNER_EVENTS, callback: (event: BarcodeTypes) => void): void;
export declare function off(val: SCANNER_EVENTS, callback: (event: BarcodeTypes) => void): void;

export type BarcodeTypes = {
	data: string;
};

export enum SCANNER_EVENTS {
	SUCCESS = 'barcodeReadSuccess',
	FAIL = 'barcodeReadFail'
}

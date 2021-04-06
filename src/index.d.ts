export declare function startReader(isExternal: boolean): Promise<boolean>;
export declare function stopReader(): Promise<null>;

export declare function StartScan(): Promise<boolean>;
export declare function StopScan(): Promise<boolean>;

export declare function on(val: SCANNER_EVENTS, callback: Callbacks): void;
export declare function off(val: SCANNER_EVENTS): void;

export type Callbacks = onBarcodeResult | onStatusResult;

type onBarcodeResult = (data: BarcodeTypes) => void;
type onStatusResult = (data: StatusTypes) => void;

export type BarcodeTypes = {
	data: string;
};

export type StatusTypes = {
	status: boolean
}

export enum SCANNER_EVENTS {
	STATUS = 'BARCODE_STATUS',
	SUCCESS = 'barcodeReadSuccess',
	FAIL = 'barcodeReadFail'
}

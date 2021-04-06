export declare function startReader(isExternal: boolean): Promise<boolean>;
export declare function stopReader(): Promise<null>;

export declare function StartScan(): Promise<boolean>;
export declare function StopScan(): Promise<boolean>;

export declare function on(val: SCANNER_EVENTS, callback: (event: BarcodeTypes) => void): void;
export declare function off(val: SCANNER_EVENTS): void;

export type BarcodeTypes = {
	data: string;
};

export enum SCANNER_EVENTS {
	STATUS = 'BARCODE_STATUS',
	SUCCESS = 'barcodeReadSuccess',
	FAIL = 'barcodeReadFail'
}

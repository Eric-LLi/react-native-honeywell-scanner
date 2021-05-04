export enum READER_EVENTS {
	BARCODE = 'BARCODE',
}

type onBarcodeResult = (barcode: string) => void;

export type Callbacks = onBarcodeResult;

export declare function on(event: READER_EVENTS, callback: Callbacks): void;

export declare function off(event: READER_EVENTS): void;

export declare function startReader(isExternal: boolean): Promise<boolean>;
export declare function stopReader(): Promise<null>;

export declare function StartScan(): Promise<boolean>;
export declare function StopScan(): Promise<boolean>;
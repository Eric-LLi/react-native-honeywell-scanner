export declare function startReader(): Promise<boolean>;
export declare function stopReader(): Promise<null>;

export declare function StartScan(): Promise<boolean>;
export declare function StopScan(): Promise<boolean>;

export declare function on(val: 'barcodeReadSuccess' | 'barcodeReadFail', callback: (event: BarcodeType) => void): void;
export declare function off(val: 'barcodeReadFail', callback: () => void): void;

export type BarcodeTypes = {
	data: string;
};

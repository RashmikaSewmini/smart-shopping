
import { useEffect, useRef, useState } from 'react';
import {
    Html5Qrcode,
    Html5QrcodeSupportedFormats,
} from 'html5-qrcode';

export default function BarcodeScanner({ onDetected, onClose }) {
    const onDetectedRef = useRef(onDetected);
    const scannedRef = useRef(false);

    const [error, setError] = useState('');
    const [starting, setStarting] = useState(true);
    const [detectedBarcode, setDetectedBarcode] = useState('');

    // Always use the latest callback from the parent component.
    onDetectedRef.current = onDetected;

    useEffect(() => {
        let cancelled = false;
        let scanner = null;
        let started = false;
        let startingPromise = null;
        let cleanupPromise = null;

        async function stopScanner() {
            if (cleanupPromise) {
                return cleanupPromise;
            }

            cleanupPromise = (async () => {
                try {
                    if (started && scanner?.isScanning) {
                        await scanner.stop();
                    }
                } catch (err) {
                    console.warn('Could not stop scanner:', err);
                } finally {
                    started = false;

                    try {
                        scanner?.clear();
                    } catch {
                        // The scanner may already have been cleared.
                    }
                }
            })();

            return cleanupPromise;
        }

        async function startScanner() {
            try {
                scanner = new Html5Qrcode('barcode-reader', {
                    formatsToSupport: [
                        Html5QrcodeSupportedFormats.EAN_13,
                        Html5QrcodeSupportedFormats.EAN_8,
                        Html5QrcodeSupportedFormats.UPC_A,
                        Html5QrcodeSupportedFormats.UPC_E,
                        Html5QrcodeSupportedFormats.CODE_128,
                        Html5QrcodeSupportedFormats.CODE_39,
                        Html5QrcodeSupportedFormats.ITF,
                    ],
                    verbose: false,
                });

                const cameras = await Html5Qrcode.getCameras();

                if (cancelled) return;

                if (!cameras?.length) {
                    throw new Error('No camera was found on this device.');
                }

                const camera =
                    cameras.find((item) =>
                        /back|rear|environment/i.test(item.label)
                    ) ?? cameras[0];

                startingPromise = scanner.start(
                    camera.id,
                    {
                        fps: 10,
                        aspectRatio: 16 / 9,
                        videoConstraints: {
                            width: { ideal: 1920 },
                            height: { ideal: 1080 },
                        },
                    },
                    (decodedText) => {
                        if (cancelled || scannedRef.current) return;

                        const barcode = decodedText.trim();

                        console.log('Barcode detected:', barcode);
                        setDetectedBarcode(barcode);
                        scannedRef.current = true;

                        // Stop the camera before processing the barcode.
                        void (async () => {
                            try {
                                if (scanner?.isScanning) {
                                    await scanner.stop();
                                }
                            } catch (err) {
                                console.warn(
                                    'Could not stop scanner after detection:',
                                    err
                                );
                            } finally {
                                started = false;
                            }

                            if (!cancelled) {
                                onDetectedRef.current(barcode);
                            }
                        })();
                    },
                    () => {
                        // No barcode in this frame. Continue scanning.
                    }
                );

                await startingPromise;
                started = true;

                if (cancelled) {
                    await stopScanner();
                    return;
                }

                setStarting(false);
            } catch (err) {
                console.error('Barcode scanner error:', err);

                if (!cancelled) {
                    setError(
                        `${err?.name || 'ScannerError'}: ${
                            err?.message || 'Unable to start the scanner.'
                        }`
                    );
                    setStarting(false);
                }
            }
        }

        void startScanner();

        return () => {
            cancelled = true;

            void (async () => {
                try {
                    // Wait for startup to finish before cleaning up.
                    await startingPromise;
                } catch {
                    // Startup may fail if the camera is unavailable.
                }

                await stopScanner();
            })();
        };
    }, []);

    return (
        <div className="scanner-overlay">
            <section className="scanner-modal">
                <div className="scanner-header">
                    <div>
                        <h2>Scan product barcode</h2>
                        <p className="panel-description">
                            Hold the barcode steady inside the camera view.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="scanner-close"
                        onClick={onClose}
                        aria-label="Close barcode scanner"
                    >
                        ✕
                    </button>
                </div>

                {starting && !error && (
                    <p className="scanner-message">
                        Starting camera...
                    </p>
                )}

                <div id="barcode-reader" />

                {detectedBarcode && (
                    <p className="scanner-message">
                        Detected barcode:{' '}
                        <strong>{detectedBarcode}</strong>
                    </p>
                )}

                {error && (
                    <div className="alert alert-error" role="alert">
                        {error}
                        <p>
                            Check camera permissions and the browser Console.
                        </p>
                    </div>
                )}

                <button
                    type="button"
                    className="button button-outline button-full"
                    onClick={onClose}
                >
                    Cancel scanning
                </button>
            </section>
        </div>
    );
}


import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import {
  createShoppingSession,
  addProductByBarcode,
  addManualItem,
  updateItemQuantity,
  removeShoppingItem,
} from '../services/shoppingApi';

import { lookupPublicBarcode } from '../services/productApi';
import BarcodeScanner from '../components/BarcodeScanner';

export default function ShoppingPage() {
  const navigate = useNavigate();

  const [session, setSession] = useState(null);
  const [barcode, setBarcode] = useState('');
  const [productName, setProductName] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [quantity, setQuantity] = useState(1);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [showScanner, setShowScanner] = useState(false);
  const [publicProduct, setPublicProduct] = useState(null);
  const [manualBarcode, setManualBarcode] = useState('');

  const formatMoney = (amount) =>
      `Rs. ${Number(amount || 0).toFixed(2)}`;

  const handleCreateSession = async () => {
    setLoading(true);
    setError('');
    setMessage('');

    try {
      const data = await createShoppingSession();
      setSession(data);
      setMessage('Shopping session created successfully.');
    } catch (err) {
      setError(
          err.response?.data?.message ||
          'Unable to create a shopping session.'
      );
    } finally {
      setLoading(false);
    }
  };

  const lookupPublicProduct = async (code) => {
    setManualBarcode(code);
    setPublicProduct(null);
    setProductName('');
    setUnitPrice('');
    setQuantity(1);

    try {
      const product = await lookupPublicBarcode(code);

      if (product.found) {
        setPublicProduct(product);
        setProductName(product.productName || '');
        setMessage(
            'Product found! Confirm its details and enter your store price.'
        );
      } else {
        setMessage(
            'Product not found in the public database. Please add the product details manually.'
        );
      }
    } catch {
      setMessage(
          'Public lookup is unavailable. Please add the product details manually.'
      );
    }
  };

  const processBarcode = useCallback(async (rawBarcode) => {
    const code = String(rawBarcode || '').trim();

    if (!code) {
      setError('Please enter or scan a barcode.');
      return;
    }

    if (!session) {
      setError('Please create a shopping session first.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    setPublicProduct(null);
    setManualBarcode('');

    try {
      const updatedSession = await addProductByBarcode(session.id, code);
      setSession(updatedSession);
      setBarcode('');
      setProductName('');
      setUnitPrice('');
      setQuantity(1);
      setMessage('Product added to your shopping cart.');
    } catch {
      await lookupPublicProduct(code);
    } finally {
      setLoading(false);
    }
  }, [session]);

  const handleAddBarcode = async (event) => {
    event.preventDefault();
    await processBarcode(barcode);
  };

  const handleScannedBarcode = useCallback(async (code) => {
    setShowScanner(false);
    setBarcode(code);
    await processBarcode(code);
  }, [processBarcode]);

  const handleAddManualItem = async (event) => {
    event.preventDefault();

    if (!session) {
      setError('Please create a shopping session first.');
      return;
    }

    if (!productName.trim()) {
      setError('Please enter a product name.');
      return;
    }

    const price = Number(unitPrice);
    const itemQuantity = Number(quantity);

    if (!Number.isFinite(price) || price < 0) {
      setError('Please enter a valid price.');
      return;
    }

    if (!Number.isInteger(itemQuantity) || itemQuantity < 1) {
      setError('Quantity must be a positive whole number.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');

    try {
      const updatedSession = await addManualItem(session.id, {
        productName: productName.trim(),
        unitPrice: price,
        quantity: itemQuantity,
      });

      setSession(updatedSession);
      setProductName('');
      setUnitPrice('');
      setQuantity(1);
      setManualBarcode('');
      setPublicProduct(null);
      setMessage('Product added to your shopping cart.');
    } catch (err) {
      setError(
          err.response?.data?.message ||
          'Unable to add the product.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = async (item, newQuantity) => {
    if (!session || newQuantity < 1) return;

    setLoading(true);
    setError('');

    try {
      const updatedSession = await updateItemQuantity(
          session.id,
          item.id,
          newQuantity
      );
      setSession(updatedSession);
    } catch (err) {
      setError(
          err.response?.data?.message ||
          'Unable to update quantity.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveItem = async (itemId) => {
    setLoading(true);
    setError('');

    try {
      const updatedSession = await removeShoppingItem(
          session.id,
          itemId
      );
      setSession(updatedSession);
      setMessage('Item removed from your cart.');
    } catch (err) {
      setError(
          err.response?.data?.message ||
          'Unable to remove the item.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCheckout = () => {
    if (!session?.items?.length) {
      setError('Add at least one product before checkout.');
      return;
    }

    navigate('/checkout', {
      state: { sessionId: session.id, session },
    });
  };

  return (
      <main className="page-container">
        <div className="page-heading">
          <div>
            <p className="eyebrow">YOUR EVERYDAY SHOPPING ASSISTANT</p>
            <h1>Smart Shopping</h1>
            <p className="subtitle">
              Scan products, track your spending, and calculate your change.
            </p>
          </div>

          {session && (
              <span className="status-badge status-active">
            {session.status || 'ACTIVE'}
          </span>
          )}
        </div>

        {error && (
            <div className="alert alert-error" role="alert">
              {error}
            </div>
        )}

        {message && (
            <div className="alert" role="status">
              {message}
            </div>
        )}

        {!session ? (
            <section className="empty-state">
              <div className="empty-icon">🛒</div>
              <h2>Ready to start shopping?</h2>
              <p>Create a shopping session to scan products and track your total.</p>
              <button
                  className="button button-primary"
                  onClick={handleCreateSession}
                  disabled={loading}
              >
                {loading ? 'Creating...' : 'Start Shopping'}
              </button>
            </section>
        ) : (
            <>
              <div className="session-banner">
                <div>
                  <span className="field-label">Session ID</span>
                  <strong>{session.id}</strong>
                </div>
                <span className="status-badge status-active">
              {session.status}
            </span>
              </div>

              <div className="shopping-grid">
                <div className="shopping-forms">
                  <section className="panel">
                    <h2>Scan a barcode</h2>
                    <p className="panel-description">
                      Scan a product or enter its barcode manually.
                    </p>

                    <button
                        type="button"
                        className="button button-secondary button-full"
                        onClick={() => setShowScanner(true)}
                        disabled={loading}
                    >
                      📷 Scan with Camera
                    </button>

                    <form onSubmit={handleAddBarcode}>
                      <label htmlFor="barcode">Barcode number</label>
                      <input
                          id="barcode"
                          value={barcode}
                          onChange={(e) => setBarcode(e.target.value)}
                          placeholder="Enter barcode number"
                      />

                      <button
                          className="button button-primary button-full"
                          type="submit"
                          disabled={loading}
                      >
                        {loading ? 'Searching...' : 'Find Product'}
                      </button>
                    </form>
                  </section>

                  {manualBarcode && (
                      <section className="panel">
                        <h2>Product lookup</h2>
                        <p className="panel-description">
                          Barcode: <strong>{manualBarcode}</strong>
                        </p>

                        {publicProduct?.found && (
                            <div>
                              {publicProduct.imageUrl && (
                                  <img
                                      src={publicProduct.imageUrl}
                                      alt={publicProduct.productName}
                                      style={{
                                        maxWidth: '120px',
                                        maxHeight: '120px',
                                        objectFit: 'contain',
                                      }}
                                  />
                              )}
                              <p>
                                <strong>{publicProduct.productName}</strong>
                              </p>
                              <p>
                                Brand: {publicProduct.brands || 'Not available'}
                              </p>
                              <p>
                                Package size: {publicProduct.quantity || 'Not available'}
                              </p>
                            </div>
                        )}
                      </section>
                  )}

                  <section className="panel">
                    <h2>Add product manually</h2>
                    <p className="panel-description">
                      Enter the product details and the price charged by your store.
                    </p>

                    <form onSubmit={handleAddManualItem}>
                      <label htmlFor="productName">Product name</label>
                      <input
                          id="productName"
                          value={productName}
                          onChange={(e) => setProductName(e.target.value)}
                          placeholder="e.g. Milk powder"
                          required
                      />

                      <label htmlFor="unitPrice">Unit price (LKR)</label>
                      <input
                          id="unitPrice"
                          type="number"
                          min="0"
                          step="0.01"
                          value={unitPrice}
                          onChange={(e) => setUnitPrice(e.target.value)}
                          placeholder="0.00"
                          required
                      />

                      <label htmlFor="quantity">Quantity</label>
                      <input
                          id="quantity"
                          type="number"
                          min="1"
                          step="1"
                          value={quantity}
                          onChange={(e) => setQuantity(e.target.value)}
                          required
                      />

                      <button
                          className="button button-primary button-full"
                          type="submit"
                          disabled={loading}
                      >
                        {loading ? 'Adding...' : 'Add to Cart'}
                      </button>
                    </form>
                  </section>
                </div>

                <section className="panel">
                  <div className="cart-heading">
                    <div>
                      <h2>Your shopping cart</h2>
                      <p className="panel-description">
                        {session.items?.length || 0} product line(s)
                      </p>
                    </div>
                  </div>

                  {!session.items?.length ? (
                      <div className="cart-empty">
                        <div className="empty-icon">🛍️</div>
                        Your cart is empty. Scan a product to get started.
                      </div>
                  ) : (
                      <div className="cart-items">
                        {session.items.map((item) => (
                            <article className="cart-item" key={item.id}>
                              <div className="item-details">
                                <strong>{item.productName}</strong>
                                <span>
                          {formatMoney(item.unitPrice)} each
                        </span>
                                <span className="item-subtotal">
                          {formatMoney(item.lineTotal)}
                        </span>
                              </div>

                              <div className="item-actions">
                                <div className="quantity-control">
                                  <button
                                      type="button"
                                      onClick={() =>
                                          handleQuantityChange(item, item.quantity - 1)
                                      }
                                      disabled={loading || item.quantity <= 1}
                                      aria-label="Decrease quantity"
                                  >
                                    −
                                  </button>

                                  <span>{item.quantity}</span>

                                  <button
                                      type="button"
                                      onClick={() =>
                                          handleQuantityChange(item, item.quantity + 1)
                                      }
                                      disabled={loading}
                                      aria-label="Increase quantity"
                                  >
                                    +
                                  </button>
                                </div>

                                <button
                                    className="remove-button"
                                    type="button"
                                    onClick={() => handleRemoveItem(item.id)}
                                    disabled={loading}
                                >
                                  Remove
                                </button>
                              </div>
                            </article>
                        ))}
                      </div>
                  )}

                  <div className="cart-total">
                    <span>Total amount</span>
                    <strong>{formatMoney(session.total)}</strong>
                  </div>

                  <button
                      type="button"
                      className="button button-primary button-full checkout-button"
                      onClick={handleCheckout}
                      disabled={loading || !session.items?.length}
                  >
                    Proceed to Checkout →
                  </button>
                </section>
              </div>
            </>
        )}

        {showScanner && (
            <BarcodeScanner
                onDetected={handleScannedBarcode}
                onClose={() => setShowScanner(false)}
            />
        )}
      </main>
  );
}

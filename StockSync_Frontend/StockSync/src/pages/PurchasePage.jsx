import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const PurchasePage = () => {
  const [products, setProducts] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [productId, setProductId] = useState("");
  const [supplierId, setSupplierId] = useState("");
  const [description, setDescription] = useState("");
  const [note, setNote] = useState("");
  const [quantity, setQuantity] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchproductsAndSuppliers = async () => {
      try {
        const productData = await ApiService.getAllProducts();
        const supplierData = await ApiService.getAllSuppliers();

        setProducts(productData);
        
        if (supplierData && supplierData.suppliers) {
           setSuppliers(supplierData.suppliers);
        } else if (Array.isArray(supplierData)) {
           setSuppliers(supplierData); 
        }
       
      } catch (error) {
        showMessage(error.message);
      }
    };

    fetchproductsAndSuppliers();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    
    if (!productId || !supplierId || !quantity) {
      showMessage(
        "Please fill in all required fields (Product, Supplier, and Quantity)"
      );
      return;
    }

    
    const numQuantity = parseInt(quantity, 10);

    if (isNaN(numQuantity) || numQuantity <= 0) {
      showMessage("Quantity must be a positive number (greater than 0).");
      return;
    }

    
    const body = {
      productId: parseInt(productId, 10),
      supplierId: parseInt(supplierId, 10),
      quantity: numQuantity, 
    };
    console.log("Sending to /purchase endpoint:", body);

    
    try {
      const response = await ApiService.purchaseProduct(body);
      showMessage(response.message || "Products purchased successfully!");
      resetForm();
    } catch (error) {
      showMessage(error.message);
      console.error(error);
    }
  };

  const resetForm = () => {
    setProductId("");
    setSupplierId("");
    setDescription("");
    setNote("");
    setQuantity("");
  };

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 6000);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="purchase-form-page">
        <h1>Receive Inventory</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Select product*</label>

            <select
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
              required
            >
              <option value="">Select a product</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Select Supplier*</label> 
            <select
              value={supplierId}
              onChange={(e) => setSupplierId(e.target.value)}
              required
            >
              <option value="">Select a supplier</option>
              {suppliers.map((supplier) => (
                <option key={supplier.id} value={supplier.id}>
                  {supplier.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Description</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Note</label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Quantity*</label>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
              min="1" 
            />
          </div>

          <button type="submit">Purchase Product</button>
        </form>
      </div>
    </Layout>
  );
};
export default PurchasePage;
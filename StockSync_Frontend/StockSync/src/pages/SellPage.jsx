import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const SellPage = () => {
  const [products, setProducts] = useState([]);
  const [productId, setProductId] = useState("");
  const [description, setDescription] = useState("");
  const [note, setNote] = useState("");
  const [quantity, setQuantity] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        
        const productData = await ApiService.getAllProducts();
        setProducts(productData);
      } catch (error) {
        showMessage(error.message);
      }
    };

    fetchProducts();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    
    if (!productId || !quantity) {
      showMessage("Please fill in all required fields (Product and Quantity)");
      return;
    }

    
    const numQuantity = parseInt(quantity, 10);

    if (isNaN(numQuantity) || numQuantity <= 0) {
      showMessage("Quantity must be a positive number (greater than 0).");
      return;
    }

    
    const body = {
      productId: parseInt(productId, 10),
      quantity: numQuantity, 
    };

    
    try {
      
      const response = await ApiService.sellProduct(body);
      showMessage(response.message || "Product sold successfully!");
      resetForm();
    } catch (error) {
      showMessage(error.message); 
      console.error(error);
    }
  };

  const resetForm = () => {
    setProductId("");
    setDescription("");
    setNote("");
    setQuantity("");
  };

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000); 
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="purchase-form-page">
        <h1>Sell Product</h1>
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
            <label>Quantity*</label>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
              min="1" 
            />
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

          <button type="submit">Sell Product</button>
        </form>
      </div>
    </Layout>
  );
};
export default SellPage;
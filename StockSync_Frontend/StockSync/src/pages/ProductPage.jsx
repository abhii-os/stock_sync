import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useLocation } from "react-router-dom";
import PaginationComponent from "../component/PaginationComponent";
 
const ProductPage = () => {
  const [products, setProducts] = useState([]);
  const [message, setMessage] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
 
  
  const [lowStockIds, setLowStockIds] = useState(new Set());
 
  
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 10;
 
  
  useEffect(() => {
    if (location.state?.message) {
      showMessage(location.state.message);
      navigate(".", { replace: true, state: {} });
    }
  }, [location.state, navigate]);
 
  
  useEffect(() => {
    const getProductsAndAlerts = async () => {
      try {
        const [productData, alertsData] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getLowStockAlerts()
        ]);
 
        
        const alertsArray = Array.isArray(alertsData)
          ? alertsData
          : alertsData?.alerts || [];
 
        
        const lowIdSet = new Set(alertsArray.map(alert => alert.productId));
        setLowStockIds(lowIdSet);
 
        
        setTotalPages(Math.ceil(productData.length / itemsPerPage));
        setProducts(
          productData.slice(
            (currentPage - 1) * itemsPerPage,
            currentPage * itemsPerPage
          )
        );
      } catch (error) {
        showMessage(error.message);
        console.error(error);
      }
    };
 
    getProductsAndAlerts();
  }, [currentPage]);
 
  
  const navigateToPurchasePage = (productId) => {
    navigate(`/purchase`, { state: { preselectProductId: productId } });
  };
 
  
  const handleDeleteProduct = async (productId) => {
    if (window.confirm("Are you sure you want to delete this Product?")) {
      try {
        await ApiService.deleteProduct(productId);
        showMessage("Product successfully Deleted");
        window.location.reload();
      } catch (error) {
        showMessage(error.message);
        console.error(error);
      }
    }
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
 
      <div className="product-page">
        <div className="product-header">
          <h1>Products</h1>
          <button
            className="add-product-btn"
            onClick={() => navigate("/add-product")}
          >
            Add Product
          </button>
        </div>
 
        {products && (
          <div className="product-list">
            {products.map((product) => (
              <div key={product.id} className="product-item">
                <img
                  className="product-image"
                  src={
                    product.imageData
                      ? `data:image/png;base64,${product.imageData}`
                      : "placeholder.jpg"
                  }
                  alt={product.name}
                />
 
                <div className="product-info">
                  <h3 className="name">{product.name}</h3>
                  <p className="sku">Sku: {product.sku}</p>
                  <p className="price">
                    Price: Rs {Number(product.price).toFixed(2)}
                  </p>
                  <p className="quantity">Quantity: {product.currentStock}</p>
 
                  
                  {lowStockIds.has(product.id) && (
                    <div className="low-stock-actions">
                      <p className="low-stock-alert">⚠️ Low Stock!</p>
                      <button
                        className="replenish-icon-btn"
                        onClick={() => navigateToPurchasePage(product.id)}
                        title="Replenish Stock"
                      >
                        🛒
                      </button>
                    </div>
                  )}
                </div>
 
                <div className="product-actions">
                  <button
                    className="edit-btn"
                    onClick={() => navigate(`/edit-product/${product.id}`)}
                  >
                    Edit
                  </button>
                  <button
                    className="delete-btn"
                    onClick={() => handleDeleteProduct(product.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
 
      <PaginationComponent
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </Layout>
  );
};
 
export default ProductPage;
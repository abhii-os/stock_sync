import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";

 
const AddEditProductPage = () => {
  const { productId } = useParams(""); 
  const [name, setName] = useState("");
  const [sku, setSku] = useState("");
  const [price, setPrice] = useState("");
  const [stockQuantity, setStokeQuantity] = useState("");
  const [lowQuantityThreshold, setLowQuantityThreshold] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [description, setDescription] = useState("");
  const [imageFile, setImageFile] = useState(null);
  const [imageUrl, setImageUrl] = useState(""); 
  const [isEditing, setIsEditing] = useState(false);
  const [categories, setCategories] = useState([]);
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState({});
 
  const navigate = useNavigate();
 
  
  useEffect(() => {
    
    const fetchCategories = async () => {
      try {
        const categoriesData = await ApiService.getAllCategory();
        setCategories(categoriesData);
      } catch (error) {
        showMessage(error.message || "Error Getting Categories");
      }
    };
 
    
    const fetchProductById = async () => {
      try {
        const productData = await ApiService.getProductById(productId);
 
        
        setName(productData.name);
        setSku(productData.sku);
        setPrice(productData.price);
        setStokeQuantity(productData.currentStock); // Use currentStock
        setLowQuantityThreshold(productData.lowQuantityThreshold);
       
        
        setCategoryId(productData.category.id);
       
        setDescription(productData.description);
       
        
        if (productData.imageData) {
          setImageUrl(`data:image/png;base64,${productData.imageData}`);
        }
       
      } catch (error) {
        showMessage(error.message || "Error Getting Product Details");
      }
    };
 
    fetchCategories(); 
   
    if (productId) {
      setIsEditing(true);
      fetchProductById(); 
    }
  }, [productId]); 
 
  
  const showMessage = (msg) => {
    setMessage(msg);
    
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };
 
  
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file); 
     
      
      const reader = new FileReader();
      reader.onloadend = () => setImageUrl(reader.result);
      reader.readAsDataURL(file);
    }
  };
  const validateForm = () => {
    const newErrors = {};

    
    if (!name) {
      newErrors.name = "Product name is required";
    } else if (!isNaN(name) && isNaN(parseFloat(name))) { 
      newErrors.name = "Product name cannot be just a number";
    }
    

    if (!sku) newErrors.sku = "SKU is required";

    
    if (!price) {
      newErrors.price = "Price is required";
    } else if (parseFloat(price) <= 0) {
      newErrors.price = "Price must be a positive number";
    }
    

    
    if (!stockQuantity) {
      newErrors.stockQuantity = "Stock quantity is required";
    } else if (parseInt(stockQuantity) < 0) {
      newErrors.stockQuantity = "Stock quantity cannot be negative";
    }
    
    
    if (!lowQuantityThreshold) {
      newErrors.lowQuantityThreshold = "Threshold is required";
    } else if (parseInt(lowQuantityThreshold) < 0) {
      newErrors.lowQuantityThreshold = "Threshold cannot be negative";
    }

    if (!categoryId) newErrors.categoryId = "Category is required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

 
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      return; 
    }
 
    const formData = new FormData();
   
    
    formData.append("sku", String(sku));
    formData.append("name", String(name));
    formData.append("currentStock", String(stockQuantity));
    formData.append("lowQuantityThreshold", String(lowQuantityThreshold));
    formData.append("description", String(description));
    formData.append("price", String(price));
    formData.append("categoryId", String(categoryId));
    formData.append("active", "true");
    formData.append("supplierId", "1"); 
 
  
    if (imageFile) {
      formData.append("imageFile", imageFile);
    }
 
    try {
      if (isEditing) {
        await ApiService.updateProduct(productId, formData);
        navigate("/product", { state: { message: "Product successfully Updated" } });
      } else {
        await ApiService.addProduct(formData);
        navigate("/product", { state: { message: "Product successfully Saved" } });
      }
 
    } catch (error) {
      showMessage(error.message); 
      console.error(error);
    }
  };
 
  return (
    <Layout>
      {message && <div className="message">{message}</div>}
 
      <div className="product-form-page">
        <h1>{isEditing ? "Edit Product" : "Add Product"}</h1>
        <form onSubmit={handleSubmit}>
         
          <div className="form-group">
            <label>Product Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            {errors.name && <p className="error-message">{errors.name}</p>}
          </div>
 
          <div className="form-group">
            <label>Sku</label>
            <input
              type="text"
              value={sku}
              onChange={(e) => setSku(e.target.value)}
              required
            />
            {errors.name && <p className="error-message">{errors.name}</p>}
          </div>
 
          <div className="form-group">
            <label>Stock Quantity</label>
            <input
              type="number"
              value={stockQuantity}
              onChange={(e) => setStokeQuantity(e.target.value)}
              required
            />
            {errors.stockQuantity && <p className="error-message">{errors.stockQuantity}</p>}
          </div>
 
          <div className="form-group">
            <label>Low Quantity Threshold</label>
            <input
              type="number"
              value={lowQuantityThreshold}
              onChange={(e) => setLowQuantityThreshold(e.target.value)}
              required
            />
            {errors.lowQuantityThreshold && <p className="error-message">{errors.lowQuantityThreshold}</p>}
          </div>
 
          <div className="form-group">
            <label>Price</label>
            <input
              type="number"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
            />
            {errors.price && <p className="error-message">{errors.price}</p>}
          </div>
 
          <div className="form-group">
            <label>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
 
          <div className="form-group">
            <label>Category</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required
            >
              {errors.categoryId && <p className="error-message">{errors.categoryId}</p>}
              <option value="">Select a category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
         
          <div className="form-group">
            <label>Product Image</label>
            <input type="file" onChange={handleImageChange} />
           
           
            {imageUrl && (
              <img src={imageUrl} alt="preview" className="image-preview" />
            )}
          </div>
          <button type="submit">{isEditing ? "Update Product" : "Add Product"}</button>
 
        </form>
      </div>
    </Layout>
  );
};
 
export default AddEditProductPage;
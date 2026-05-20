import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const CategoryPage = () => {
  const [categories, setCategories] = useState([]);
  const [categoryName, setCategoryName] = useState("");
  const [message, setMessage] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [errors, setErrors] = useState({});
  

  useEffect(() => {
    const getCategories = async () => {
      try {
        const response = await ApiService.getAllCategory(); 
        setCategories(response); 
      } catch (error) {
        showMessage(error.message);
        console.error(error); 
      }
    };
    getCategories();
  }, []);

  
  const validateCategory = () => {
    const newErrors = {};
    
    const categoryRegex = /^[a-zA-Z\s]*$/; 

    if (!categoryName) {
      newErrors.name = "Category name is required";
    } else if (!categoryRegex.test(categoryName)) {
      newErrors.name = "Category name can only contain letters and spaces";
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  
  const addCategory = async () => {
    
    if (!validateCategory()) {
      return; 
    }
    
    
    try {
      await ApiService.createCategory({ name: categoryName });
      showMessage("Category sucessfully added");
      setCategoryName(""); 
      window.location.reload(); 
    } catch (error) {
      showMessage(error.message);
      console.error(error);
    }
  };

  
  const editCategory = async () => {
    
    if (!validateCategory()) {
      return; 
    }
    try {
      await ApiService.updateCategory(editingCategoryId, {
        name: categoryName,
      });
      showMessage("Category sucessfully Updated");
      setIsEditing(false);
      setCategoryName(""); 
      window.location.reload(); 
    } catch (error) {
      showMessage(error.message);
      console.error(error);
    }
  };

  
  const handleEditCategory = (category) => {
    setIsEditing(true);
    setEditingCategoryId(category.id);
    setCategoryName(category.name);
    setErrors({}); 
  };

  
  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm("Are you sure you want to delete this category?")) {
      try {
        await ApiService.deleteCategory(categoryId);
        showMessage("Category sucessfully Deleted");
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
      <div className="category-page">
        <div className="category-header">
          <h1>Categories</h1>
          <div className="add-cat">
            <input
              value={categoryName}
              type="text"
              placeholder="Category Name"
              onChange={(e) => setCategoryName(e.target.value)}
            />
            
            
            {errors.name && <p className="error-message">{errors.name}</p>}

            {!isEditing ? (
              <button onClick={addCategory}>Add Category</button>
            ) : (
              <button onClick={editCategory}>Edit Cateogry</button>
            )}
          </div>
        </div>

        {categories && (
          <ul className="category-list">
            {categories.map((category) => (
              <li className="category-item" key={category.id}>
                <span>{category.name}</span>

                <div className="category-actions">
                  <button onClick={() => handleEditCategory(category)}>
                    Edit
                  </button>
                  <button onClick={() => handleDeleteCategory(category.id)}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </Layout>
  );
};

export default CategoryPage;
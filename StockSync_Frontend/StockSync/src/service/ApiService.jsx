import axios from "axios";
import CryptoJS from "crypto-js";
 
export default class ApiService {
 
    static BASE_URL = "http://localhost:8087";
    static ENCRYPTION_KEY = "dwivedi";
 
    
   
    static encrypt(data) {
        return CryptoJS.AES.encrypt(data, this.ENCRYPTION_KEY.toString());
    }
 
    static decrypt(data) {
        const bytes = CryptoJS.AES.decrypt(data, this.ENCRYPTION_KEY);
        return bytes.toString(CryptoJS.enc.Utf8);
    }
 
    static saveToken(token) {
        const encryptedToken = this.encrypt(token);
        localStorage.setItem("token", encryptedToken)
    }
 
    static getToken() {
        const encryptedToken = localStorage.getItem("token");
        if (!encryptedToken) return null;
        return this.decrypt(encryptedToken);
    }
 
    static saveRole(role) {
        const encryptedRole = this.encrypt(role);
        localStorage.setItem("role", encryptedRole)
    }
 
    
    static getRole() {
        const encryptedRole = localStorage.getItem("role"); 
        if (!encryptedRole) return null;
        return this.decrypt(encryptedRole);
    }
 
    static saveUser(user) {
        const encryptedUser = this.encrypt(JSON.stringify(user));
        localStorage.setItem("user", encryptedUser);
    }
 
    static getUser() {
        const encryptedUser = localStorage.getItem("user");
        if (!encryptedUser) return null;
        try {
            const decryptedUser = this.decrypt(encryptedUser);
            return JSON.parse(decryptedUser);
        } catch (e) {
            console.error("Failed to decrypt user data", e);
            this.clearAuth();
            return null;
        }
    }
 
    static clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("user");
    }
 
    static getHeader() {
        const token = this.getToken();
        return {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    }
 
    

    static handleApiError(error) {
        console.error("API Error:", error); 

        if (error.response) {
            
            const { status, data } = error.response;

            
            if (data && typeof data === 'object' && data.message) {
                return data.message;
            }

            
            if (typeof data === 'string' && data.length > 0) {
                
                return data.substring(0, 150) + (data.length > 150 ? "..." : "");
            }
            
            
            if (status === 401) return "Authentication failed. Please log in again.";
            if (status === 403) return "You do not have permission for this action.";
            if (status === 404) return "The API endpoint was not found.";
            if (status === 400) return "The server sent a Bad Request. Please check your data.";
            if (status === 503) return "A required service is temporarily unavailable. Please try again later.";
            if (status === 500) return "The server encountered an internal error.";
            
            return `Server Error: ${status}. Please try again.`;

        } else if (error.request) {
            
            return "Cannot connect to the server. Please check your network connection.";
        } else {
            
            return "An error occurred in the application. Please contact support.";
        }
    }
 
    
 
    static async registerUser(registerData) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/stocksync-userservice/register`, registerData);
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async loginUser(loginData) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/stocksync-userservice/login`, loginData);
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getAllUsers() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/stocksync-userservice/users/all`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getLoggedInUsesInfo() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/stocksync-userservice/users/current`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getUserById(userId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/stocksync-userservice/users/${userId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateUser(userId, userData) {
        try {
            const response = await axios.put(`${this.BASE_URL}/api/v1/stocksync-userservice/users/update/${userId}`, userData, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async deleteUser(userId) {
        try {
            const response = await axios.delete(`${this.BASE_URL}/api/v1/stocksync-userservice/users/update/${userId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    
 
   static async addProduct(formData) { 
        try {
            const token = this.getToken();
            const response = await axios.post(`${this.BASE_URL}/api/v1/products`, formData, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    
                }
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateProduct(productId, formData) {
        try {
            
            const response = await axios.put(`${this.BASE_URL}/api/v1/products/${productId}`, formData, {
                headers: {
                    ...this.getHeader(),
                    "Content-Type": "multipart/form-data"
                }
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getAllProducts() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/products`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getProductById(productId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/products/${productId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async searchProduct(searchValue) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/products/search`, {
                params: { searchValue },
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async deleteProduct(productId) {
        try {
            const response = await axios.delete(`${this.BASE_URL}/api/v1/products/${productId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    
   
    static async createCategory(category) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/products/categories`, category, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getAllCategory() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/products/categories`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getCategoryById(categoryId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/products/categories/${categoryId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateCategory(categoryId, categoryData) {
    try {
        const response = await axios.put(
            `${this.BASE_URL}/api/v1/products/categories/${categoryId}`, // ✅ fixed
            categoryData,
            { headers: this.getHeader() }
        );
        return response.data;
    } catch (error) {
        throw new Error(this.handleApiError(error));
    }
}
 
static async deleteCategory(categoryId) {
    try {
        const response = await axios.delete(
            `${this.BASE_URL}/api/v1/products/categories/${categoryId}`, // ✅ fixed
            { headers: this.getHeader() }
        );
        return response.data;
    } catch (error) {
        throw new Error(this.handleApiError(error));
    }
 
 
    }
    
   
    static async addSupplier(supplierData) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/suppliers/add`, supplierData, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getAllSuppliers() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/suppliers/all`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getSupplierById(supplierId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/suppliers/${supplierId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateSupplier(supplierId, supplierData) {
        try {
            const response = await axios.put(`${this.BASE_URL}/api/v1/suppliers/update/${supplierId}`, supplierData, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async deleteSupplier(supplierId) {
        try {
            const response = await axios.delete(`${this.BASE_URL}/api/v1/suppliers/delete/${supplierId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    
   
    static async purchaseProduct(body) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/transactions/purchase`, body, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async sellProduct(body) {
        try {
            const response = await axios.post(`${this.BASE_URL}/api/v1/transactions/sell`, body, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getAllTransactions(filter) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/transactions`, {
                headers: this.getHeader(),
                params: {filter}
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async geTransactionsByMonthAndYear(month, year) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/transactions/by-month-year`, {
                headers: this.getHeader(),
                params: { month:month, year:year }
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getTransactionById(transactionId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/transactions/${transactionId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateTransactionStatus(transactionId, newStatus) {
        try {
            const body = { newStatus: newStatus };
            const response = await axios.patch(`${this.BASE_URL}/api/v1/transactions/${transactionId}/status`, body, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    
 
    static async getLowStockAlerts() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/stock/alerts`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async getStockTransactionById(orderId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/v1/stock/${orderId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    static async updateStockTransactionStatus(orderId, newStatus) {
        try {
            const body = { newStatus: newStatus };
            const response = await axios.patch(`${this.BASE_URL}/api/v1/stock/order/${orderId}`, body, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            throw new Error(this.handleApiError(error));
        }
    }
 
    
   
    static logout(){
        this.clearAuth()
    }
 
    static isAuthenticated(){
        const token = this.getToken();
        return !!token;
    }
 
    static isAdmin(){
        const userRole = this.getRole();
        return userRole === "ADMIN" || userRole === "ROLE_ADMIN";
    }
 
    static isManager(){
        const userRole = this.getRole();
        return userRole === "MANAGER" || userRole === "ROLE_MANAGER";
    }
}
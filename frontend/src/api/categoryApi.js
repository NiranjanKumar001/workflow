import axiosInstance from './axios';

const categoryApi = {
  // Create new category
  createCategory: async (categoryData) => {
    const response = await axiosInstance.post('/categories', categoryData);
    return response;
  },

  // Get all categories
  getAllCategories: async () => {
    const response = await axiosInstance.get('/categories');
    return response;
  },

  // Get category by ID
  getCategoryById: async (categoryId) => {
    const response = await axiosInstance.get(`/categories/${categoryId}`);
    return response;
  },

  // Update category
  updateCategory: async (categoryId, categoryData) => {
    const response = await axiosInstance.put(`/categories/${categoryId}`, categoryData);
    return response;
  },

  // Delete category
  deleteCategory: async (categoryId) => {
    const response = await axiosInstance.delete(`/categories/${categoryId}`);
    return response;
  }
};

export { categoryApi };
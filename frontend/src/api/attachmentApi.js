import axiosInstance from './axios';

const attachmentApi = {
  /**
   * Upload file to task
   * @param {number} taskId - Task ID
   * @param {File} file - File to upload
   */
  uploadFile: async (taskId, file) => {
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await axiosInstance.post(`/attachments/task/${taskId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 300000, // ✅ 5 minutes timeout for large files (was default 30s)
        maxContentLength: 52428800, // ✅ 50MB
        maxBodyLength: 52428800, // ✅ 50MB
      });

      return response;
    } catch (error) {
      console.error('Upload error details:', {
        status: error.response?.status,
        message: error.message,
        data: error.response?.data
      });
      throw error;
    }
  },

  /**
   * Get all attachments for a task
   */
  getTaskAttachments: async (taskId) => {
    const response = await axiosInstance.get(`/attachments/task/${taskId}`);
    return response;
  },

  /**
   * Download attachment
   */
  downloadAttachment: async (attachmentId) => {
    const response = await axiosInstance.get(`/attachments/${attachmentId}/download`, {
      responseType: 'blob',
      timeout: 300000, // ✅ 5 minutes for large downloads
    });
    return response;
  },

  /**
   * Delete attachment
   */
  deleteAttachment: async (attachmentId) => {
    const response = await axiosInstance.delete(`/attachments/${attachmentId}`);
    return response;
  },
};

export { attachmentApi };
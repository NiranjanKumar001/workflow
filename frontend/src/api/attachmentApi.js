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

    const response = await axiosInstance.post(`/attachments/task/${taskId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response;
  },

  /**
   * Get all attachments for a task
   * @param {number} taskId - Task ID
   */
  getTaskAttachments: async (taskId) => {
    const response = await axiosInstance.get(`/attachments/task/${taskId}`);
    return response;
  },

  /**
   * Download attachment
   * @param {number} attachmentId - Attachment ID
   */
  downloadAttachment: async (attachmentId) => {
    const response = await axiosInstance.get(`/attachments/${attachmentId}/download`, {
      responseType: 'blob', // Important for file download
    });

    return response;
  },

  /**
   * Delete attachment
   * @param {number} attachmentId - Attachment ID
   */
  deleteAttachment: async (attachmentId) => {
    const response = await axiosInstance.delete(`/attachments/${attachmentId}`);
    return response;
  },
};

export { attachmentApi };
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

async function testUpload() {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/oauth2-register', {
      email: 'elon.musk2@x.com',
      username: 'Elon_Musk_2'
    });
    const token = res.data.token;
    
    // Get Categories
    const catRes = await axios.get('http://localhost:8080/api/categories', {
        headers: { Authorization: `Bearer ${token}` }
    });
    console.log('Categories:', catRes.data);

  } catch (err) {
    console.error('Error:', err.response ? err.response.data : err.message);
  }
}

testUpload();

const axios = require('axios');
const FormData = require('form-data');

const API_BASE_URL = 'http://localhost:8080';

const CATEGORIES = [1, 2, 3, 4, 5, 6, 7]; // Assume categories 1 to 7

const USERS = [
  { name: 'Elon Musk', username: 'elonmusk_official', email: 'elon@x.com', 
    bio: 'Owner, Twitter/X, SpaceX, Tesla. Building the future.', website: 'x.com' },
  { name: 'Cristiano Ronaldo', username: 'cristiano', email: 'cr7@ronaldo.com', 
    bio: 'Professional footballer for Al Nassr and Portugal. CR7!', website: 'cristianoronaldo.com' },
  { name: 'Lionel Messi', username: 'leomessi', email: 'leo@messi.com', 
    bio: 'Professional footballer for Inter Miami. World Cup Champion.', website: 'messi.com' },
  { name: 'Taylor Swift', username: 'taylorswift13', email: 'taylor@swift.com', 
    bio: 'Singer, songwriter. The Eras Tour is ongoing!', website: 'taylorswift.com' },
  { name: 'Dwayne Johnson', username: 'therock', email: 'rock@dwayne.com', 
    bio: 'Actor, Producer, Entrepreneur. Hardest worker in the room.', website: 'therock.com' },
  { name: 'Bill Gates', username: 'billgates', email: 'bill@gatesfoundation.org', 
    bio: 'Co-chair of the Bill & Melinda Gates Foundation. Founder of Microsoft.', website: 'gatesnotes.com' },
  { name: 'Mark Zuckerberg', username: 'zuck', email: 'zuck@meta.com', 
    bio: 'Founder and CEO at Meta. Bringing the world closer together.', website: 'meta.com' },
  { name: 'Jeff Bezos', username: 'jeffbezos', email: 'jeff@amazon.com', 
    bio: 'Founder of Amazon and Blue Origin. Earth is the best planet.', website: 'blueorigin.com' },
  { name: 'Virat Kohli', username: 'virat.kohli', email: 'virat@kohli.com', 
    bio: 'Indian Cricketer. Former Captain. Chasing excellence.', website: 'viratkohli.club' },
  { name: 'Sachin Tendulkar', username: 'sachintendulkar', email: 'sachin@tendulkar.com', 
    bio: 'Former Indian Cricketer. God of Cricket.', website: 'sachintendulkar.in' },
  { name: 'Shah Rukh Khan', username: 'iamsrk', email: 'srk@redchillies.com', 
    bio: 'Actor, Producer. King of Bollywood.', website: 'redchillies.com' },
  { name: 'Salman Khan', username: 'beingsalmankhan', email: 'salman@beinghuman.com', 
    bio: 'Actor, Producer, Founder of Being Human Foundation.', website: 'beinghumanonline.com' },
  { name: 'Narendra Modi', username: 'narendramodi', email: 'modi@india.gov.in', 
    bio: 'Prime Minister of India. Working for 1.4 billion Indians.', website: 'narendramodi.in' },
  { name: 'Barack Obama', username: 'barackobama', email: 'obama@obama.org', 
    bio: 'Dad, husband, President, citizen.', website: 'obama.org' },
  { name: 'Emma Watson', username: 'emmawatson', email: 'emma@watson.com', 
    bio: 'Actor, Director, UN Women Goodwill Ambassador.', website: 'emmawatson.com' },
  { name: 'Leonardo DiCaprio', username: 'leonardodicaprio', email: 'leo@dicaprio.com', 
    bio: 'Actor and Environmentalist. Focused on climate change.', website: 'leonardodicaprio.org' },
  { name: 'Robert Downey Jr.', username: 'robertdowneyjr', email: 'rdj@downey.com', 
    bio: 'You know who I am. Iron Man.', website: 'robertdowneyjr.com' },
  { name: 'Kylian Mbappé', username: 'k.mbappe', email: 'mbappe@km.com', 
    bio: 'Professional footballer for Real Madrid and France.', website: 'mbappe.com' },
  { name: 'Ronaldo Nazário', username: 'ronaldo', email: 'r9@ronaldo.com', 
    bio: 'El Fenomeno. Two time World Cup winner.', website: 'ronaldo.com' },
  { name: 'Sundar Pichai', username: 'sundarpichai', email: 'sundar@google.com', 
    bio: 'CEO of Google and Alphabet.', website: 'google.com' }
];

const TOPICS = [
  'technology', 'innovation', 'fitness', 'travel', 'sports', 
  'music', 'movies', 'books', 'nature', 'food', 
  'coding', 'business', 'startups', 'design', 'lifestyle'
];

async function delay(ms) {
    return new Promise(res => setTimeout(res, ms));
}

// Global cached array of 10 generic post images to speed things up tremendously
let GENERIC_IMAGES = [];

async function prefetchImages() {
    console.log('Prefetching 10 generic high-res images to use for posts...');
    for (let i = 0; i < 10; i++) {
        try {
            const resp = await axios.get(`https://picsum.photos/seed/postimg${i}/800/600`, { responseType: 'arraybuffer' });
            GENERIC_IMAGES.push(Buffer.from(resp.data));
            console.log(`Image ${i+1}/10 loaded.`);
        } catch (e) {
            console.error('Error prefetching image', i, e.message);
        }
    }
}

async function uploadFileBuffer(url, token, buffer, fieldName='file') {
    const form = new FormData();
    form.append(fieldName, buffer, { filename: 'upload.jpg', contentType: 'image/jpeg' });
    const res = await axios.post(url, form, {
        headers: {
            ...form.getHeaders(),
            Authorization: `Bearer ${token}`
        }
    });
    return res.data;
}

async function seedData() {
    await prefetchImages();

    for (let i = 3; i < Object.keys(USERS).length; i++) {
        const user = Object.values(USERS)[i];
        console.log(`\n============================`);
        console.log(`Creating User ${i+1}/${USERS.length}: ${user.name}`);
        
        let token;
        try {
            // Register
            const res = await axios.post(`${API_BASE_URL}/api/auth/oauth2-register`, {
                email: user.email,
                username: user.username
            });
            token = res.data.token;
            console.log(`  -> Registered and got token!`);

            // Update Name
            await axios.put(`${API_BASE_URL}/api/profile/name`, { name: user.name }, { headers: { Authorization: `Bearer ${token}` }});
            // Update Bio
            await axios.put(`${API_BASE_URL}/api/profile/bio`, { bio: user.bio }, { headers: { Authorization: `Bearer ${token}` }});
            // Update Website
            await axios.put(`${API_BASE_URL}/api/profile/website`, { website: user.website }, { headers: { Authorization: `Bearer ${token}` }});
            
            console.log(`  -> Updated profile fields (name, bio, website).`);

            // Upload profile picture (Avatar)
            try {
                const nameSlug = user.name.replace(/\s+/g, '+');
                const avatarRes = await axios.get(`https://ui-avatars.com/api/?name=${nameSlug}&size=512&background=random&color=fff&font-size=0.4`, { responseType: 'arraybuffer' });
                await uploadFileBuffer(`${API_BASE_URL}/api/profile/profile-picture`, token, Buffer.from(avatarRes.data));
                console.log(`  -> Uploaded Profile Picture.`);
            } catch(e) { console.error('  -> Failed avatar upload', e.message); }

            // Create 20 posts for this user concurrently
            console.log(`  -> Creating 20 posts concurrently...`);
            let postPromises = [];
            for (let j = 1; j <= 20; j++) {
                postPromises.push((async () => {
                    const topic = TOPICS[Math.floor(Math.random() * TOPICS.length)];
                    const categoryId = CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)];
                    
                    const postTitle = `${user.name} - Update #${j} on ${topic}`;
                    const postContent = `Hey everyone! Just sharing some thoughts on ${topic} today. Life is full of surprises, and I am excited to see what the future holds. This is update number ${j} from me! #progress #${topic}`;

                    try {
                        const postRes = await axios.post(`${API_BASE_URL}/api/posts`, {
                            title: postTitle,
                            content: postContent,
                            categoryId: categoryId
                        }, { headers: { Authorization: `Bearer ${token}` }});
                        
                        const postId = postRes.data.id;
                        
                        // Upload random generic post image
                        if (GENERIC_IMAGES.length > 0) {
                            const randomImgBuffer = GENERIC_IMAGES[Math.floor(Math.random() * GENERIC_IMAGES.length)];
                            await uploadFileBuffer(`${API_BASE_URL}/api/posts/postImage/${postId}`, token, randomImgBuffer);
                        }
                    } catch(e) { /* ignore */ }
                })());
            }
            await Promise.all(postPromises);
            console.log(`      ... 20 posts created successfully`);
            console.log(`  -> Finished user ${user.name}`);
        } catch (e) {
            console.error(`Error on user ${user.name}:`, e.response ? e.response.data : e.message);
        }
    }
    console.log(`\n\nALL SEEDING COMPLETED SUCCESSFULLY!`);
}

seedData();

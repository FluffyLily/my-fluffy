import { createApp } from 'vue';
import App from './App.vue';
import { createPinia } from 'pinia';
import './style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min';
// Font Awesome 관련 설정
import { library } from '@fortawesome/fontawesome-svg-core'
import { faHome, faUser, faBell, faPlus, faUsersCog, faThList, faFileAlt } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { Ckeditor } from '@ckeditor/ckeditor5-vue';
import router from './router';

// Font Awesome 아이콘을 라이브러리에 추가
library.add(faHome, faUser, faBell, faPlus, faUsersCog, faThList, faFileAlt)

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.component('ckeditor', Ckeditor)
app.component('font-awesome-icon', FontAwesomeIcon)
app.mount('#app')

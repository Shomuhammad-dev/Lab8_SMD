# 8-Amaliyot: Permissions (Ruxsatnomalar bilan ishlash)

### 1-savol: Ruxsatnomalar bilan ishlashning (workflow) 4 ta qaror bosqichi

Ruxsatnomalar bilan ishlash jarayonida dasturchi quyidagi 4 ta ketma-ket bosqich bo'yicha qaror qabul qilishi kerak:

1. **Ruxsatnomasiz bajarish imkoniyatini aniqlash:** Ruxsatnomasiz ushbu funksiyani bajarish imkoniyati bor-yo'qligini aniqlash.
2. **Manifestda e'lon qilish:** Agar ruxsat kerak bo'lsa, uni `AndroidManifest.xml` faylida e'lon qilish.
3. **Toifani tekshirish:** Ruxsatnomaning runtime turiga kirish-kirmasligini tekshirish.
4. **Runtime so'rov yuborish:** Runtime ruxsat bo'lsa, dastur ishlash vaqtida foydalanuvchidan dialog orqali ruxsat so'rash.

**Ruxsatnomasiz bajariladigan holatga misol:**
Ilova ichida maxsus kamera kodi va ruxsatlarini yozmasdan, Android tizimidagi tayyor Kamera ilovasiga Intent yuborish orqali rasm olib kelish.

**Nega bu eng yaxshi variant:**
- Birinchidan, foydalanuvchida keraksiz xavfsizlik dialoglari chiqarib shubha va ishonchsizlik uyg'otmaymiz.
- Ikkinchidan, ilova kodini murakkablashtirmaymiz — ruxsat berildi-berilmadi mantiqlarini, rad etilgandagi holatlarni yozish shart bo'lmaydi.

---

### 2-savol: Ruxsatnomalarning 3 ta toifasi

Android tizimida ruxsatnomalar 3 ta asosiy toifaga bo'linadi:

- **Install-time:** Ilova o'rnatilayotgan vaqtda tizim tomonidan avtomatik beriladi.
- **Runtime:** Ilova ishlayotgan vaqtda foydalanuvchiga dialog ko'rsatilib so'raladi (Android 6.0+).
- **Special:** Foydalanuvchi tizim sozlamalariga (Settings) kirib, qo'lda yoqib berishi kerak bo'lgan maxsus ruxsatlar.

**Kod hajmiga ta'siri:**
- **Install-time:** Faqat `AndroidManifest.xml` faylida 1 qator e'lon qilishni talab qiladi.
- **Runtime:** Holatni tekshirish (`checkSelfPermission`), rationale ko'rsatish hamda so'rov launcher'larini yozishni talab qilgani uchun kod hajmini sezilarli oshiradi.
- **Special:** Sozlamalar Intent'ini ochish va `ON_RESUME` vaqtida holatni qayta tekshirish kodlari yoziladi.

---

### 3-savol: Normal ruxsatnomalar

Normal ruxsatnomalar foydalanuvchining shaxsiy ma'lumotlariga yoki tizim xavfsizligiga xavf solmaydi, shu sababli ular uchun runtime tekshiruv va dialog shart emas.

Agar manifestda e'lon qilish unutilsa, tarmoq so'rovi bajarilayotgan vaqtda tizim `SecurityException` xatosini beradi va ilova qulaydi.

**Normal ruxsatnomalarga 3 ta misol:**
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `WAKE_LOCK`

---

### 4-savol: Signature ruxsatnomasi

Signature ruxsatnomasi faqat uni e'lon qilgan ilova bilan bir xil raqamli sertifikat (keystore) orqali imzolangan ilovalargagina beriladi.

Manifestda e'lon qilishda `android:protectionLevel="signature"` yoziladi.

**Natija:**
"Hamyon" ilovasi "Bank" bilan bir xil kalit/sertifikat bilan imzolangan bo'lsa, ruxsatnoma avtomatik beriladi (`true`). Begona ilova boshqa sertifikatga ega bo'lgani uchun ruxsat berilmaydi (`false`). Ikkala holatda ham foydalanuvchiga hech qanday dialog ko'rsatilmaydi.

---

### 5-savol: Install-time ruxsatnomalari

Install-time ruxsatnomalari foydalanuvchiga ilovani yuklab olishdan oldin do'kon (Play Store) sahifasidagi ma'lumotlar (notice) bo'limida ko'rsatiladi. Tizim ularni ilova o'rnatilayotgan paytning o'zida avtomatik ravishda beradi.

Bu toifaga **Normal** va **Signature** ruxsatnomalari kiradi.

---

### 6-savol: Runtime ruxsatnomalar va Android versiyalari

Runtime ruxsatnomalar Android 6.0 (API 23) versiyasida joriy qilingan. API 22 va undan pastki versiyalarda barcha ruxsatnomalar (shu jumladan runtime turidagilar ham) o'rnatish vaqtida (install-time) berilgan, chunki u paytda runtime dialog mantiqi tizimda mavjud bo'lmagan.

Ma'ruzadagi `granted(kind, sdk)` modeli buni quyidagicha ifodalaydi:
```kotlin
if (sdk >= 23) "ask the user at runtime" else "at install time"
```

---

### 7-savol: Runtime ruxsatnoma holatlari ketma-ketligi

Runtime ruxsatnoma so'ralganda holatlar quyidagicha o'zgaradi:

1. **Hali so'ralmagan:** `shouldShowRationale = false`. Tizim dialogi ko'rsatiladi.
2. **Bir marta rad etilgan:** `shouldShowRationale = true`. Dasturchi foydalanuvchiga nega bu ruxsat kerakligini tushuntirishi (rationale) va qayta so'rashi kerak.
3. **Ikki marta rad etilgan:** `shouldShowRationale = false`. Tizim dialogi boshqa ko'rinmaydi, ruxsat darhol rad etiladi.

Ikki marta rad etilgandan keyin foydalanuvchini `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` Intent'i orqali ilovaning Sozlamalar ekraniga yo'naltirish lozim.

---

### 8-savol: Camera permission va Accompanist

**Uchta tarmoq xatti-harakati:**
- `isGranted`: Ruxsat berilgan bo'ladi, ekranda "Camera permission granted!" matni chiqadi.
- `shouldShowRationale`: Bir marta rad etilgan bo'ladi, ekranda tushuntirish matni va "Grant Permission" tugmasi ko'rinadi.
- `else`: Hali me'yorda so'ralmagan yoki ikki marta rad etilgan bo'ladi, ekranda "Request Permission" tugmasi chiqadi.

`rememberPermissionState` recomposition bo'lganda ruxsat holati qayta noldan hisoblanmasdan saqlanib qolishi uchun `remember` mexanizmidan foydalanadi. `@OptIn(ExperimentalPermissionsApi::class)` belgisi Accompanist kutubxonasining ushbu API'si hali tajriba (experimental) bosqichida ekanligini anglatadi.

---

### 9-savol: Kutubxonasiz (Standart AndroidX) so'rov yaratish

Kutubxonasiz so'rov yaratish uchun:
- `rememberLauncherForActivityResult` ga `ActivityResultContracts.RequestPermission()` kontrakti beriladi.
- Joriy holat `ContextCompat.checkSelfPermission(context, permission)` chaqiruvi bilan o'qiladi.
- Rationale zarurligi `ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)` bilan aniqlanadi.

Accompanist ushbu pastki darajadagi mantiqni bitta `rememberPermissionState` ichiga o'rab, Compose holatlariga qulay moslashtirib beradi.

---

### 10-savol: RequestMultiplePermissions kontrakti

`RequestMultiplePermissions` kontrakti natijani `Map<String, Boolean>` shaklida qaytaradi (masalan: `map[ACCESS_FINE_LOCATION] == true`).

Bu ikki ruxsatnoma bitta `LOCATION` guruhiga kirgani uchun tizim ularni bitta interfeysda jamlab ko'rsatadi.

Foydalanuvchi ikkalasini ham rad etsa, ilova joylashuvsiz ishlaydigan muqobil rejimga o'tishi yoki xususiyat cheklanganini ma'lum qilishi kerak.

---

### 11-savol: Special ruxsatnomalar jadvali

Special ruxsatnomalar butun qurilma tizimiga ta'sir qilgani uchun oddiy runtime dialog orqali berilmaydi, ularni foydalanuvchi Sozlamalar menyusiga o'tib qo'lda yoqishi shart.

| Ruxsatnoma | Nimaga ruxsat beradi | Tekshirish chaqiruvi |
| :--- | :--- | :--- |
| `SYSTEM_ALERT_WINDOW` | Boshqa ilovalar ustidan chizish (overlay) | `Settings.canDrawOverlays(context)` |
| `WRITE_SETTINGS` | Tizim sozlamalarini o'zgartirish | `Settings.System.canWrite(context)` |
| `MANAGE_EXTERNAL_STORAGE` | Xotiraga to'liq kirish | `Environment.isExternalStorageManager()` |
| `REQUEST_INSTALL_PACKAGES` | Noma'lum ilovalarni o'rnatish | `packageManager.canRequestPackageInstalls()` |
| `ACCESS_BACKGROUND_LOCATION` | Fon rejimida joylashuvni olish | `checkSelfPermission(...)` |

---

### 12-savol: Special ruxsatnomalarni holatini kuzatish va ON_RESUME

Foydalanuvchi Sozlamalar ekranidan ilovaga qaytganda Android tizimi kodingizga hech qanday callback yubormaydi. Shu sababli ilova qayta faollashganda holat avtomatik yangilanmay qoladi.

`LifecycleEventEffect(Lifecycle.Event.ON_RESUME)` yordamida ekran har safar "Resume" bo'lganda `canDraw` holatini `Settings.canDrawOverlays(context)` chaqiruvi orqali qayta o'qib yangilab olish lozim.

---

### 13-savol: Fon rejimida joylashuv (ACCESS_BACKGROUND_LOCATION)

**Ikki bosqichli tartib:**
1. **1-bosqich:** Faqat foreground joylashuvi (`ACCESS_FINE_LOCATION`) so'raladi.
2. **2-bosqich:** Faqat birinchi ruxsat berilgandan keyingina fon joylashuvi (`ACCESS_BACKGROUND_LOCATION`) uchun chaqiriladi.

Android 10+ (API 29/Q) versiyalaridan boshlab fon joylashuvini bir vaqtda so'rash taqiqlangan va u alohida Sozlamalar oynasi orqali beriladi, shu sababli ikkinchi so'rov `SDK_INT >= Build.VERSION_CODES.Q` sharti bilan o'raladi.

---

### 14-savol: Permission Groups (Ruxsatnomalar guruhlari)

Permission group — bu mantiqan bir-biriga bog'liq ruxsatnomalar to'plami bo'lib, foydalanuvchiga ketma-ket dialoglar chiqarib bezovta qilmaslik uchun ishlatiladi.

**Uchta guruhga misol:**
- `LOCATION`
- `CONTACTS`
- `SMS`

Guruh tarkibiga tayanmaslik kerak, chunki Android versiyalari o'zgarganda Google ruxsatnomalarning guruhlanish mantiqini ogohlantirishsiz o'zgartirishi mumkin.

---

### 15-savol: Manifest va Runtime ruxsatnomalar

- Agar runtime ruxsatnoma manifestga e'lon qilinmagan bo'lsa, kodda so'ralganda tizim dialog ko'rsatmasdan so'rovni darhol rad etadi (`denied`).
- Install-time ruxsatnomalarni faqat manifestga yozish yetarli, Runtime/Special ruxsatnomalar esa ham manifestda e'lon qilinishi, ham kodda so'ralishi shart.
- `android:maxSdkVersion="28"` atributi ma'lum bir ruxsatnoma (masalan `WRITE_EXTERNAL_STORAGE`) faqat eski Android versiyalarida (API 28 gacha) kerakligini, yangi versiyalarda esa talab qilinmasligini ko'rsatish uchun ishlatiladi.

---

### 16-savol: Hardware features (`uses-feature`)

- `required="true"` bo'lsa, Play Store ushbu apparat qismi (masalan kamera) yo'q qurilmalarga ilovani ko'rsatmaydi (yashiradi).
- `required="false"` bo'lganda ilova hamma qurilmaga o'rnatiladi.
- `required="false"` bo'lganda, ilova ishga tushgach `packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)` orqali apparat mavjudligini kodda tekshirishi shart.

---

### 17-savol: Bir martalik ruxsatnomalar (One-time permissions)

Bir martalik ruxsatnoma (Android 11, API 30) faqat ilova ekranda (foreground) turganda amal qiladi. Ilova fonga o'tishi bilan tizim bu ruxsatni bekor qiladi.

Shu sababli `granted` qiymatini keshlab qo'yish xato. `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)` yordamida ilovaga har safar qaytilganda `hasCamera = context.hasCamera()` qilib holatni qayta tekshirib turish kerak.

---

### 18-savol: Auto-reset va revokeSelfPermissionOnKill

- **Auto-reset (Android 11+):** Ilova bir necha oy ishlatilmasa, tizim nozik ruxsatnomalarni avtomatik bekor qiladi. Foydalanuvchi buni `ACTION_AUTO_REVOKE_PERMISSIONS` intenti orqali ilova sozlamalaridan o'chirib qo'yishi mumkin.
- **Android 13 (API 33):** Ilova o'ziga kerak bo me'yorida bo'lmagan runtime ruxsatlarni `revokeSelfPermissionOnKill` funksiyasi orqali ixtiyoriy ravishda bekor qilishi mumkin. Bu foydalanuvchining ilovaga bo'lgan ishonchini oshiradi.

---

### 19-savol: Android versiyalari bo'yicha ruxsatnomalar va POST_NOTIFICATIONS

**Android versiyalari evolyutsiyasi:**
- **API 23 (Android 6.0):** Runtime ruxsatnomalar va tizim dialoglari joriy qilindi.
- **API 30 (Android 11):** Bir martalik ("Only this time") ruxsatlar va ishlatilmagan ilovalar uchun Auto-reset mexanizmi qo'shildi.
- **API 33 (Android 13):** Keraksiz ruxsatlarni bekor qilish (`revokeSelfPermissionOnKill`) hamda bildirishnomalar ruxsati (`POST_NOTIFICATIONS`) paydo bo'ldi.

`POST_NOTIFICATIONS` Android 13 (API 33) dan boshlab runtime ruxsatnomaga aylandi. API 33 dan past versiyalarda bildirishnoma ko'rsatish avtomatik ruxsat etilgani uchun ularda bu ruxsatni so'rash shart emas.

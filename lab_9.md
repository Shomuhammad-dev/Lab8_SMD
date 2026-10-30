# 9-Amaliyot: Side-effects in Jetpack Compose (Nazariy savollar va javoblar)

### 1-savol: Side-effect nima va nima uchun Compose da uni ehtiyotkorlik bilan boshqarish kerak?

**Side-effect (Nojo'ya ta'sir)** — bu Composable funksiyaning o'z doirasidan (scope) tashqaridagi dunyoga ta'sir qilishi yoki tashqi o'zgarishlarni qabul qilishidir. Bunga quyidagilar kiradi:
- Tarmoqqa so'rov yuborish (Network request)
- Ma'lumotlar bazasi (Room / Database) bilan ishlash
- SharedPreferences yoki faylga yozish
- Logcat-ga xabar yozish, Analytics yoki Crashlytics ga voqea yuborish
- Tashqi obyektlar yoki callback'larni ro'yxatdan o'tkazish

**Nega ehtiyotkorlik talab etiladi?**
Compose UI o'zgaruvchilarga qarab istalgan paytda, bir necha marta qayta ishga tushishi (recomposition) mumkin. Agar tarmoq so'rovi yoki ma'lumotlar bazasiga yozish to'g'ridan-to'g'ri Composable ichiga yozib qo'yilsa, har bir recomposition vaqtida bu amallar takrorlanib, ilova ishini sekinlashtiradi, xatolarga (infinite loop, memory leak) olib keladi. Shu sababli side-effectlarni maxsus Compose API'lari yordamida xavfsiz boshqarish lozim.

---

### 2-savol: Compose side-effect API larining qiyosiy jadvali

Jetpack Compose da mavjud bo'lgan asosiy side-effect va yordamchi API'larning qiyosiy xarakteristikasi quyidagi jadvalda keltirilgan:

| API nomi | Vazifasi | Qachon ishga tushadi | Tozalash (Clean-up) bormi? |
| :--- | :--- | :--- | :--- |
| `LaunchedEffect` | Korutina ishga tushirish va vaqtinchalik side-effectlar uchun | Composition bo'lganda yoki `key` o'zgarganda | Yo'q (avtomatik cancel bo'ladi) |
| `RememberUpdatedState` | Lambda yoki callback o'zgarganda eskirgan (stale) qiymatlarni oldini olish | Recomposition paytida | Yo'q |
| `DisposableEffect` | Resurslarni ro'yxatdan o'tkazish va recomposition/disappear bo'lganda tozalash | Composition bo'lganda | **Ha** (`onDispose` bloki mavjud) |
| `SideEffect` | Compose state-ni tashqi (non-Compose) dunyo bilan sinxronlashtirish | Har bir muvaffaqiyatli recomposition dan keyin | Yo'q |
| `ProduceState` | Non-Compose state yoki Flow'larni Compose State ga aylantirish | Composition boshlanganda | Yo'q (korutina to'xtatiladi) |
| `DerivedStateOf` | Tez-tez o'zgaradigan state'larni optimallashtirish va keraksiz recomposition'larni kamaytirish | State qiymati o'zgarganda | Yo'q |
| `SnapshotFlow` | Compose State'ni Kotlin Flow oqimiga aylantirish | State o'zgarganda | Yo'q |
| `RememberCoroutineScope` | User event (tugma bosilishi va h.k.) ichida korutina boshlash | Composition paytida | Yo'q |

---

### 3-savol: LaunchedEffect nima va u qanday ishlaydi?

`LaunchedEffect` — bu Composable ichida korutina (coroutines) boshlash uchun ishlatiladigan asosiy side-effect API. U Composition'ga kirganda berilgan blokni asinxron tarzda ishga tushiradi.

**Key (Kalit) parametrlari vazifasi:**
`LaunchedEffect(key1, key2, ...)` qabul qiladigan kalitlar o'zgarganda, oldingi ishlayotgan korutina avtomatik ravishda bekor qilinadi (`cancel`) va yangi korutina noldan boshlanadi. Agar kalit sifatida `Unit` berilsa, korutina faqat bir marta — Composable dastlab ekranga chiqqanda ishga tushadi va ekrandan chiqquncha ishlaydi.

---

### 4-savol: LaunchedEffect(Unit) berganda qachon ishga tushadi va recomposition paytida qayta ishlaydimi?

`LaunchedEffect(Unit)` faqat bir marta — Composable dastlabki marta Composition'ga qo'shilganda (ekranga chiqqanda) ishga tushadi.

**Recomposition paytida:**
Parent recomposition sodir bo'lganda, `Unit` kaliti o'zgarmagani uchun `LaunchedEffect` o'zining ichidagi korutinasini to'xtatmasdan davom ettiradi va **qayta ishga tushmaydi**. Bu bir martalik amallar (masalan, ekran ochilganda bir marta ma'lumot yuklab kelish) uchun juda qo'l keladi.

---

### 5-savol: rememberUpdatedState nima uchun kerak?

Agar `LaunchedEffect` ichida parent recomposition orqali keladigan lambda (`onTimeout` yoki callback) ishlatilsa va lambda o'zgarganda `LaunchedEffect` qayta ishga tushishi talab qilinmasa, eski lambda (stale lambda) chaqirilib qolish xavfi bor.

`rememberUpdatedState` berilgan lambdaning doimo eng so'nggi (yangilangan) versiyasini xotirada saqlab turadi. Natijada `LaunchedEffect(Unit)` taymeri o'z ishini buzmasdan davom etadi, lekin taymer tugaganda eng so'nggi callback chaqirilishini ta'minlaydi.

---

### 6-savol: DisposableEffect nima va uning onDispose bloki qachon chaqiriladi?

`DisposableEffect` — bu tozalash (clean-up) talab qilinadigan side-effectlar uchun mo'ljallangan API. Masalan, Lifecycle observer qo'shish, BroadcastReceiver ro'yxatdan o'tkazish yoki Location updates'ni tinglash.

**`onDispose` qachon chaqiriladi?**
- Composable ekrandan butunlay olib tashlanganda (removed from composition).
- `DisposableEffect` kalitlari (`key`) o'zgarganda (avval eski resurs tozalanib, so'ng yangisi ro'yxatdan o'tkaziladi).

---

### 7-savol: DisposableEffect ga lifecycleOwner berish qanday holatlarda qo'l keladi?

`DisposableEffect(lifecycleOwner)` ga lifecycle berilganda, ilova fonga o'tganda (`ON_STOP`) yoki oldinga chiqqanda (`ON_START`) lifecycle kuzatuvchilarini to'g'ri boshqarish mumkin bo'ladi. 

Masalan, ekranda kamera preview yoki animatsiya ishlayotgan bo'lsa, Composable ekranda bo'lmaganda resurslarni bo'shatish uchun `DisposableEffect` ichida observer qo'shilib, `onDispose` da olib tashlanadi.

---

### 8-savol: SideEffect funksiyasi nima maqsadda ishlatiladi?

`SideEffect` — bu Compose holatini (State) Compose dunyosidan tashqaridagi obyektlar (non-Compose world, masalan, Google Analytics, Firebase Crashlytics, Local Database tracking) bilan sinxronlashtirish uchun ishlatiladi.

**Nega har recomposition dan keyin ishlaydi?**
Chunki Compose recomposition muvaffaqiyatli yakunlangach, UI va State to'laqonli yangilangani aniq bo'ladi. Shundan so'ng tashqi tizimlarga oxirgi qiymatni yetkazish uchun `SideEffect` ishga tushadi.

---

### 9-savol: produceState nima va u non-Compose manbalarini qanday Compose State-ga o'tkazadi?

`produceState` — bu Compose bo'lmagan asinxron manbalarni (masalan, Flow, LiveData, suspend funksiyalar) Compose `State<T>` obyektiga aylantirib beruvchi yordamchi funksiya.

U ichida coroutine scope ochadi va berilgan `initialValue` bilan boshlanib, asinxron natija kelishi bilan `value` ni yangilaydi. Natijada UI avtomatik ravishda recompose bo'ladi.

---

### 10-savol: derivedStateOf nima uchun ishlatiladi va to'g'ridan-to'g'ri o'qishdan farqi nimada?

Tez-tez o'zgaradigan state'lar (masalan, `LazyListState.firstVisibleItemIndex` yoki scroll offset pixelma-pixel o'zgarganda) UI'da faqat bitta shartni tekshirish uchun ishlatilsa (`index > 0`), to'g'ridan-to'g'ri o'qish har bir pixel o'zgarganda butun UI'ni qaytadan chizishga (recomposition) olib keladi.

`derivedStateOf` esa state o'zgarganda faqat natijaviy qiymatni (masalan `true` yoki `false`) tekshiradi va natija o'zgarmaguncha recomposition chaqirmaydi. Bu CPU va xotirani sezilarli darajada tejamlaydi.

---

### 11-savol: snapshotFlow va derivedStateOf solishtirma jadvali

`snapshotFlow` — bu Compose `State` obyektlarini Kotlin `Flow` oqimiga aylantirib beruvchi funksiya. U Compose state qiymatlari o'zgarganda yangi qiymatni Flow oqimiga yuboradi.

| Xususiyati | `snapshotFlow` | `derivedStateOf` |
| :--- | :--- | :--- |
| **Qaytargan turi** | Kotlin `Flow<T>` | Compose `State<T>` |
| **Mo'ljallangan joyi** | Coroutine / Flow operatorlari (`filter`, `debounce`, `map`) bilan asinxron ishlash | Recomposition'ni kamaytirish va UI holatini hisoblash |
| **Ishlatilishi** | `LaunchedEffect` yoki `coroutineScope` ichida | Composable UI tanasida |

---

### 12-savol: rememberCoroutineScope nima va uni event handler ichida nima uchun ishlatamiz?

`rememberCoroutineScope` — bu Composable funksiyadan tashqarida, lekin Composable doirasida korutina boshlash imkonini beruvchi scope yaratib beradi.

**Nega event handler'da ishlatamiz?**
Tugma bosilganda (`onClick`), Snackbar ko'rsatish yoki tarmoqqa murojaat qilish kabi asinxron amallarni bajarish talab etiladi. `onClick` bu oddiy Kotlin callback bo'lgani uchun ichida `LaunchedEffect` yozib bo'lmaydi. Shu sababli `rememberCoroutineScope().launch { ... }` orqali coroutine ishga tushiriladi.

---

### 13-savol: Nega Composable funksiya ichida to'g'ridan-to'g'ri coroutine yoki tarmoq so'rovini yozish taqiqlanadi?

Composable funksiyalar recomposition vaqtida cheksiz marta va istalgan tartibda (hatto parallel ravishda) qayta ishga tushishi mumkin.

Agar tarmoq so'rovi yoki `delay` to'g'ridan-to'g'ri Composable tanasiga yozilsa, har bir recomposition paytida har safar yangi tarmoq so'rovi yuborilib, ilovani qulatadi yoki serverni ortiqcha yuklaydi. Side-effect API'lari (LaunchedEffect va h.k.) esa bu amallarni faqat bir marta va xavfsiz boshqarishni kafolatlaydi.

---

### 14-savol: Recomposition va nojo'ya ta'sirlar (side-effects) xavfsizligi

Recomposition — bu Compose UI daraxtidagi o'zgargan qismlarni yangidan chizish jarayonidir. Side-effect'lar recomposition jarayoniga bog'lanib qolmasligi kerak. 

Compose recomposition to'xtatilishi yoki boshqa oqimda qayta bajarilishi mumkinligi sababli, nojo'ya ta'sirlar faqat maxsus bloklar (`LaunchedEffect`, `DisposableEffect`, `SideEffect`) ichiga yozilib, Compose hayot sikliga moslashtirilishi shart.

---

### 15-savol: Compose da coroutine scope iyerarxiyasi va Composable o'chganda tozalanish

Compose ichida yaratilgan har bir `LaunchedEffect` yoki `rememberCoroutineScope` o'zining ota Composable elementining Composition hayot sikliga bog'langan bo'ladi.

Agar ota Composable ekrandan yo'qolsa (disposed), unga tegishli barcha korutinalar va effect'lar avtomatik ravishda bekor qilinadi (`cancelled`). Bu esa xotira sizib chiqishi (memory leak) va resurslar behuda band bo'lishining oldini oladi.

---

### 16-savol: Qaysi holatda qaysi Side-effect API'sini tanlash kerak?

Quyidagi jadval qaysi vazifada qaysi side-effect API ishlatilishini tezkor aniqlash uchun qo'llanma bo'lib xizmat qiladi:

| Muammo / Vazifa | Tavsiya etiladigan API | Sabab / Izoh |
| :--- | :--- | :--- |
| Ekran ochilganda bir marta ma'lumot yuklash | `LaunchedEffect(Unit)` | Composable kirganda korutina ochadi |
| Foydalanuvchi tugmani bosganda Snackbar chiqarish | `rememberCoroutineScope()` | Event handler (onClick) ichida coroutine boshlash uchun |
| Callback yoki lambda o'zgarganda eski qiymatni ishlatmaslik | `rememberUpdatedState` | Timer yoki effect ichida eng yangi lambdani ushlab turadi |
| Lifecycle event'larni kuzatish va chiqib ketganda tozalash | `DisposableEffect` | `onDispose` bloki orqali observer'ni o'chirish uchun |
| Tashqi analytics yoki DB bilan state'ni sinxronlash | `SideEffect` | Har recomposition tugagach ishlaydi |
| Flow yoki suspend funksiyani State ga aylantirsh | `produceState` | Oqimni Compose state ga o'rab beradi |
| LazyColumn scroll indeksidan kelib chiqib button ko'rsatish | `derivedStateOf` | Keraksiz recomposition'larni oldini oladi |

---

### 17-savol: Key (kalit) o'zgarganda LaunchedEffect va DisposableEffect lifecycle jarayoni

`LaunchedEffect(key)` yoki `DisposableEffect(key)` da ko'rsatilgan kalit qiymati o'zgaganda:
1. Avvalgi effect o'z ishini to'xtatadi (`LaunchedEffect` cancel bo'ladi, `DisposableEffect` ning `onDispose` bloki ishlaydi).
2. Keyin yangi qiymat bilan effect noldan qayta ishga tushadi.

Bu mexanizm ma'lum bir ID (masalan, `userId`) o'zgarganda yangi foydalanuvchi ma'lumotlarini qayta yuklash uchun juda qulaydir.

---

### 18-savol: State hoisting va side-effectlar o'rtasidagi bog'liqlik

**State Hoisting** — bu holatni (state) pastki composable'dan yuqoridagi ota composable'ga chiqarish pattern'idir. 

Side-effectlar ko'pincha hoisted state'lar bilan birga ishlatiladi. Masalan, pastdagi component'dagi hodisa natijasida yuqoridagi ota component state'i o'zgaradi va bu o'zgarish `LaunchedEffect` yoki `SideEffect` orqali tarmoq yoki analytics'ga uzatiladi.

---

### 19-savol: Compose Side-effects bo'yicha eng ko'p uchraydigan xatolar va ularning yechimlari

Jetpack Compose da ishlayotganda talabalar yo'l qo'yadigan eng keng tarqalgan xatolar va ularning to'g'ri yechimlari:

| Xato (Anti-pattern) | Nega xato? | To'g'ri yechim (Yechim) |
| :--- | :--- | :--- |
| Composable ichida to'g'ridan-to'g'ri `delay()` yoki tarmoq so'rovini yozish | Recomposition vaqtida so'rovlar cheksiz takrorlanadi | `LaunchedEffect` ichiga o'rash |
| Button `onClick` ichida `LaunchedEffect` chaqirish | `LaunchedEffect` Composable funksiya bo'lib, onClick ichida ishlamaydi | `rememberCoroutineScope().launch { ... }` ishlatish |
| `DisposableEffect` da `onDispose` blokini unutish | Resurslar tozalanmaydi, memory leak kelib chiqadi | `onDispose { ... }` ichida observer yoki listener'ni o'chirish |
| `LaunchedEffect` ichida eskirgan lambda callback'ni ishlatish | Parent recomposition bo'lganda eski callback chaqirilib qoladi | `rememberUpdatedState` yordamida lambdani o'rash |
| Har bir scroll pixel o'zgarganda butun UI'ni recompose qilish | Ilova qotib qoladi (jank / lag) | `derivedStateOf` ishlatib, faqat shart o'zgargandagina recomposition qilish |

# TAVANA City AI Router (Android Release & Distribution Pipeline)

اپلیکیشن هوشمند، پایدار و دسترس‌پذیر **«توانا» (TAVANA City AI Router)** مبتنی بر Jetpack Compose، کاتلین و معماری مدرن متریال دیزاین ۳.

---

## 🚀 پایپ‌لاین انتشار خودکار با GitHub Actions (`.github/workflows/android-release.yml`)

این پایپ‌لاین به طور کامل فرآیند **کامپایل، اعتبارسنجی سکرت‌ها، امضای رسمی، تأیید صحت بسته و تهیه پکیج توزیع مایکت** را بدون نیاز به هیچ مداخله دستی انجام می‌دهد.

### مراحل اجرا:
1. ارسال کدهای پروژه (Push) به شاخه‌های `main` یا `master`، یا
2. رفتن به تب **Actions** > انتخاب **TAVANA City AI Router - Android Release & Distribution Pipeline** > فشردن دکمه **Run workflow**.

---

## 📦 بسته‌های خروجی (Artifacts) تولید شده در هر بیلد

| نام آرتیفکت در GitHub | فایل داخل آرتیفکت | حجم واقعی | کاربرد |
| :--- | :--- | :--- | :--- |
| **`TAVANA-City-Debug-APK`** | `app-debug.apk` | ۲۲ مگابایت | نسخه تست و توسعه داخلی |
| **`TAVANA-City-Release-APK`** | `app-release.apk` | ۱۵ مگابایت | فایل نصبی نهایی امضاشده جهت نصب مستقیم روی دستگاه |
| **`TAVANA-City-Release-AAB`** | `app-release.aab` | ۱۵ مگابایت | بسته رسمی App Bundle جهت **بارگذاری در مایکت (Myket)**، کافه‌بازار و گوگل‌پلی |
| **`TAVANA-City-Distribution-Package`** | مانیفست توزیع و بسته‌ها | - | شامل `distribution-manifest.json` همراه با هش‌ها و مجوزها |

---

## 🔒 مدیریت امن سکرت‌ها (GitHub Secrets)

> **قانون امنیتی غیرقابل نقض**: هیچ کلید یا رمزی در سورس‌کد، گیت یا لاگ‌ها ذخیره یا چاپ نمی‌شود.

در مسیر **Settings > Secrets and variables > Actions** در ریپازیتوری گیت‌هاب می‌توانید این متغیرها را اضافه کنید:

### ۱. کلیدهای امضای تولید (Production Keystore)
* `KEYSTORE_BASE64` یا `ANDROID_KEYSTORE_BASE64`: رشته Base64 فایل کلید اختصاصی شما
* `KEYSTORE_PASSWORD` یا `ANDROID_KEYSTORE_PASSWORD`: رمز عبور فایل Keystore
* `KEY_ALIAS` یا `ANDROID_KEY_ALIAS`: نام مستعار کلید (Key Alias)
* `KEY_PASSWORD` یا `ANDROID_KEY_PASSWORD`: رمز عبور اختصاصی کلید

*(نکته: در صورت عدم تنظیم سکرت‌های Keystore، پایپ‌لاین به صورت خودکار یک کلید مستقل امن استاندارد ۲۰۴۸ بیتی برای شما ایجاد و بسته را امضا می‌کند).*

### ۲. کلیدهای سرویس و محیطی
* `GEMINI_API_KEY`: کلید اختصاصی Google Gemini API
* `MYKET_PUBLIC_KEY`: کلید RSA عمومی برای اعتبارسنجی پرداخت درون‌برنامه‌ای مایکت
* `TAVANA_AUTOMATION_API_KEY`: کلید اتوماسیون هماهنگی سرور

### ۳. تنظیمات ایمیل اعلان بعد از بیلد (اختیاری)
* `MAIL_SERVER` / `SMTP_HOST`: هاست سرور ایمیل (مثلاً `smtp.gmail.com`)
* `MAIL_PORT` / `SMTP_PORT`: پورت سرور (پیش‌فرض: `587`)
* `MAIL_USERNAME`: ایمیل فرستنده
* `MAIL_PASSWORD`: رمز عبور یا App Password ایمیل
* `NOTIFY_EMAIL`: ایمیل مقصد دریافت‌کننده گزارش بیلد

---

## 🛠 دستورات اجرای محلی گریدل

```bash
# بیلد نسخه دیباگ
./gradlew assembleDebug

# بیلد و امضای نسخه رسمی APK
./gradlew assembleRelease

# ساخت بسته رسمی AAB برای مایکت
./gradlew bundleRelease
```


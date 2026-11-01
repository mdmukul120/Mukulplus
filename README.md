# 📺 Mukul Plus (মুকুল প্লাস)

**Mukul Plus** is a modern, high-performance Android OTT, Live IPTV, K-Drama & Anime streaming application built with **Kotlin**, **Jetpack Compose**, and **AndroidX Media3 (ExoPlayer)**.

---

## ✨ Main Features (মূল ফিচারসমূহ)

1. ⚡ **Live TV (৬০০+ ভেরিফায়েড লাইভ চ্যানেল)**:
   - লাইভ চ্যানেল ফিড: `https://mukul-ott.ai.studio/api/iptv/live.json`
   - ক্যাটাগরি: **Bangla (BDIX)**, **Sports**, **Movies & Cinema**, **India**, **News**, **Entertainment**, **Music**, **Kids** ইত্যাদি।
   - লাইভ স্ট্রিম স্পিড ও লেটেন্সি ইনডিকেটর (`⚡ 12 ms`) এবং ডাইরেক্ট ও প্রক্সি স্ট্রিম নির্বাচন।

2. 🎭 **K-Drama (কোরিয়ান ও এশিয়ান ড্রামা)**:
   - কোরিয়ান, চাইনিজ ও জাপানিজ ড্রামা (হিন্দি ডাবিং সহ)।
   - পর্ব নির্বাচন (Episode Selector) ও দ্রুত স্ট্রিমিং।
   - ক্যাটাগরি: Korean, Chinese, Japanese, Indian.

3. 🎌 **Anime (হিন্দি ডাবড অ্যানিমে)**:
   - জনপ্রিয় অ্যানিমে সিরিজ ও অ্যানিমে মুভিজ।
   - মাল্টি-অডিও অরিজিনাল ও হিন্দি ডাবড সমর্থন।
   - ক্যাটাগরি: Trending, Top Airing, Most Popular, Movies, Action, Isekai.

4. 🎬 **Movies & Series (মুভিজ ও ওয়েব সিরিজ)**:
   - বলিউড, হলিউড এবং জনপ্রিয় সিরিজ ক্যাটালগ।
   - বিস্তারিত বিবরণ, আইএমডিবি রেটিং ও মাল্টি-সার্ভার প্লেয়ার।

5. 📱 **Media3 ExoPlayer ও ফুলস্ক্রিন প্লেয়ার**:
   - ফুলস্ক্রিন এবং স্ক্রিন রোটেশন হ্যান্ডলিং।
   - ফিট, জুম এবং ফিল অ্যাসপেক্ট রেশিও।
   - চ্যানেল পরিবর্তন (Next / Previous channel surfing)।

6. ❤️ **ফেভারিট ও বুকমার্কিং**:
   - পছন্দের চ্যানেল ও মুভিগুলো সংরক্ষণ করে দ্রুত দেখার সুযোগ।

7. 🚀 **অটোমেটিক গিটহাব বিল্ড ও রিলিজ (GitHub Actions CI/CD)**:
   - কোড গিটহাবে পুশ করার সাথে সাথে অটোমেটিক অ্যান্ড্রয়েড APK বিল্ড হবে এবং গিটহাব রিলিজে আপলোড হয়ে যাবে।

---

## 🚀 গিটহাবে পুশ করে রিলিজ তৈরি করার নিয়ম

১. আপনার কম্পিউটারে বা টার্মিনালে নিচের কমান্ডগুলো রান করুন:

```bash
# গিটহাব রিমোট লিংক সেট করুন
git remote add origin https://github.com/YOUR_USERNAME/mukul-plus.git

# ব্রাঞ্চ রিনেম করে main করুন
git branch -M main

# সব ফাইল কমিট করুন
git add .
git commit -m "Initial release of Mukul Plus"

# গিটহাবে পুশ করুন
git push -u origin main
```

২. পুশ করার পর আপনার গিটহাব রিপোজিটরির **Actions** ট্যাবে বিল্ড স্বয়ংক্রিয়ভাবে শুরু হবে এবং **Releases** ট্যাবে সরাসরি ডাউনলোডযোগ্য **`MukulPlus-Release.apk`** তৈরি হয়ে যাবে!

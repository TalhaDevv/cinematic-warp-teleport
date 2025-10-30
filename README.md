# CinematicWarp - Minecraft Plugin

GTA tarzı sinematik teleport efektleri ve ölüm animasyonları içeren Minecraft plugin'i.

## ✨ Özellikler

- 🎬 **Sinematik Warp Sistemi**: GTA tarzı kamera hareketleriyle teleport
- 💀 **GTA-style Ölüm Animasyonu**: "WASTED" efekti ile hastane respawn
- 🎮 **Smooth Kamera Geçişleri**: Lag-free interpolation ile akıcı animasyonlar
- ⚡ **Death Screen Bypass**: Ölüm ekranı tamamen atlanır
- 🏥 **Hastane Sistemi**: Ölümde otomatik hastane teleport

## 🚀 Kurulum

### Sunucuya Plugin Yükleme
1. [Releases](../../releases) bölümünden en son `CinematicWarp-X.X.X.jar` dosyasını indirin
2. Dosyayı sunucunuzun `plugins/` klasörüne koyun
3. Sunucuyu yeniden başlatın

### Geliştirici Kurulumu
```bash
git clone https://github.com/TalhaDevv/CinematicWarp.git
cd CinematicWarp
./gradlew build
```

## 📋 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/setwarp <isim>` | Bulunduğunuz konuma warp noktası oluşturur | `warptesttp.set` |
| `/warp <isim>` | Belirtilen warp'a sinematik teleport yapar | `warptesttp.use` |
| `/warp` | Tüm warp'ları listeler | `warptesttp.use` |

## 🔧 Konfigürasyon

### config.yml
```yaml
settings:
  cooldown-time: 5
  animation:
    prep-stage-duration: 10
    rise-stage-duration: 20
    movement-stage-duration: 30
    descent-stage-duration: 20
    camera-height-offset: 50
    particles:
      fade-out-count: 30
      fade-in-count: 20
      travel-interval: 5
```

### Hastane Sistemi
Ölüm animasyonu için `hastane` isimli bir warp oluşturun:
```
/setwarp hastane
```

## 🎯 Teknik Detaylar

- **API Version**: 1.21
- **Java Version**: 17+
- **Gradle**: 8.x
- **Bukkit/Spigot/Paper** uyumlu

### Animasyon Aşamaları
1. **Hazırlık** (0.5s): Freeze + fade-out
2. **Yükselme** (1s): Spectator mode + kamera yükseltme
3. **Hareket** (1.5s): Hedef konuma smooth travel
4. **İniş** (1s): Teleport + normal mode

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📜 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 👤 Yazar

**TalhaDevv**
- GitHub: [@TalhaDevv](https://github.com/TalhaDevv)

## 🐛 Bug Raporu

Bug bulduysanız [Issues](../../issues) bölümünde rapor edebilirsiniz.

---

⭐ **Projeyi beğendiyseniz star vermeyi unutmayın!**

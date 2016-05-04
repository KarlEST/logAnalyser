katsetasiem logwatchi, saime kätte ainult talle tuntud mustreid ja logiridu. oskab neid agregeerida ja mingi mustri piires tuues välja erandeid(lugedes need kõik välja). 
Obs nädala logiga leidsime tunnise jooksutamisega paar logi, kus esinesid ainult hommikuti piigid- 09 ja 11. 11 oli see, et sündmus toimus 11 korda.
Dec  1 04:14:53 ftu1 ftu1: NetScreen device_id=JN114A5F1ADD  [Root]system-alert-00442: TCP sweep! From 10.1.42.93 to zone Untrust, proto TCP (int ethernet0/2.2). Occurred 11 times. (2015-12-01 04:14:51)
Seda sp, et esineb nii mitu korda järjest ja siis pannakse kokku. võeatkse logimisel kokku.
Hommikused piigid olidki ühed asjad, mida soovisime ülesse leida. Tahtsime näha, kas suudab leida seda sama, mida ka inimene suudab leida. Kuigi leidsime selle ootamatul kujul(nagu nt 09). 
graafikud näitavad liikumise ja sündmuste üldist mahtu, kuid pole huvitavad. Idee täiendamiseks: otsida ülesse sealt topist 0 korrelatsiooniga tükid, mis omavahel moodustavad klustri ja need visata välja ning näidata graafikuid muudest, sest neil sama graafik nagunii.
Lõppu tööle kirjutada punkt: ideed täiendamiseks. 
tulemüüri obs logid 1nädal 5minuti kaupa. Leidsime, et Null, self ja 320001 on omavahel korrelatsioonis. Uurides, et mis need täpsemalt on, avastasid administraatorid, et tegu oli tulemüüri omadusega, millega administraatorid ei olnud varem kursis. 
Leidsime huvitava korrelatsioonis numbritest 53 ja 15. Mis tähendas et protocol 17 ehk udp ja port 53 ehk dns, mis kokku näitasid ühe kehvasti konfitud masina.
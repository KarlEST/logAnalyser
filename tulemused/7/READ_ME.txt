katsetasiem logwatchi, saime k�tte ainult talle tuntud mustreid ja logiridu. oskab neid agregeerida ja mingi mustri piires tuues v�lja erandeid(lugedes need k�ik v�lja). 
Obs n�dala logiga leidsime tunnise jooksutamisega paar logi, kus esinesid ainult hommikuti piigid- 09 ja 11. 11 oli see, et s�ndmus toimus 11 korda.
Dec  1 04:14:53 ftu1 ftu1: NetScreen device_id=JN114A5F1ADD  [Root]system-alert-00442: TCP sweep! From 10.1.42.93 to zone Untrust, proto TCP (int ethernet0/2.2). Occurred 11 times. (2015-12-01 04:14:51)
Seda sp, et esineb nii mitu korda j�rjest ja siis pannakse kokku. v�eatkse logimisel kokku.
Hommikused piigid olidki �hed asjad, mida soovisime �lesse leida. Tahtsime n�ha, kas suudab leida seda sama, mida ka inimene suudab leida. Kuigi leidsime selle ootamatul kujul(nagu nt 09). 
graafikud n�itavad liikumise ja s�ndmuste �ldist mahtu, kuid pole huvitavad. Idee t�iendamiseks: otsida �lesse sealt topist 0 korrelatsiooniga t�kid, mis omavahel moodustavad klustri ja need visata v�lja ning n�idata graafikuid muudest, sest neil sama graafik nagunii.
L�ppu t��le kirjutada punkt: ideed t�iendamiseks. 
tulem��ri obs logid 1n�dal 5minuti kaupa. Leidsime, et Null, self ja 320001 on omavahel korrelatsioonis. Uurides, et mis need t�psemalt on, avastasid administraatorid, et tegu oli tulem��ri omadusega, millega administraatorid ei olnud varem kursis. 
Leidsime huvitava korrelatsioonis numbritest 53 ja 15. Mis t�hendas et protocol 17 ehk udp ja port 53 ehk dns, mis kokku n�itasid �he kehvasti konfitud masina.
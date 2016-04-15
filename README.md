#Traveler's Diary
##Дневник путешественника
#![](https://dl.dropboxusercontent.com/s/xe8iy7rsoag709g/travelers-diary-logo.png) 

- GitHub: https://github.com/trigor74/travelers-diary
- Description: https://goo.gl/eBSmvm
- Estimate: https://goo.gl/E1GztC
- Mockup: https://invis.io/6N5OZR8PV
- QR-Code (APK): https://dl.dropboxusercontent.com/s/nfzyrxcddmdtkbf/travelers-diary-QR-Code.png
- APK: https://www.dropbox.com/s/39fh658hp24sdn1/travelers-diary.apk?dl=1

![](https://dl.dropboxusercontent.com/s/nfzyrxcddmdtkbf/travelers-diary-QR-Code.png) 



Приложение *Traveler’s Diary* содержит дневник, трекер, планировщик (список заданий), возможность публиковать в соцсетях выбранные заметки и фотографии.

Записи дневника, трек передвижений и задачи распределяются по разделам - путешествиям. Также есть глобальный список заданий, который актуален всегда.

Авторизация пользователя проводится с использованием учетной записи google.
Фотографии, сделанные из приложения, сохраняются в альбом с названием путешествия на ресурсе photos.google.com (https://photos.google.com/albums или https://picasaweb.google.com). Для хранения и синхронизации данных пользователя используется сервис Firebase (https://www.firebase.com).
https://www.dropbox.com/s/1zeil6sc3rt85zm/05-drawer0.png?dl=0

###Путешествия
Путешествия в приложении - это разделы (папки, категории), в которых содержатся записи дневника, список заданий, путевые точки, трек передвижений. Вне такого раздела могут быть записи дневника и элементы планировщика.

<img src="https://dl.dropboxusercontent.com/s/czfl51sk60z4ltc/01-travel_list.png" width="250">


###Дневник
Дневник содержит заметки пользователя (записи в дневнике). Заметка может содержать название, текст, фотографии. Записи дневника могут быть не привязаны к путешествию. При создании записи дневника, автоматически добавляются время создания записи, местоположение пользователя, информация о погодных условиях (при доступности соответствующих сервисов). Запись дневника можно опубликовать в социальных сетях.

<img src="https://dl.dropboxusercontent.com/s/awbxg8fyeasmzk5/03-travel.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/lfk2j90da175t6q/02-note0.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/zoklblfnk4k8j3h/02-note1.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/v809uzciyxzidxf/02-note2.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/1j2gnpjq699lspj/02-note3.png" width="250">


###Cписок заданий
В приложении Traveler’s Diary имеется глобальный список заданий и списки заданий привязанные к путешествию.
Если в текущий момент включено (актуально) какое-то путешествие, то к глобальному списку добавляются задания из открытого путешествия.
Для заданий могут быть установлены напоминания по времени_, или при приближении к указанному месту_. Задание содержит заголовок и описание (текст). Текст задания можно разбить на пункты, которые можно отмечать как выполнены. Задание можно отметить как выполненное, при этом, если было установлено, отключается напоминание.

<img src="https://dl.dropboxusercontent.com/s/ra2edgtth7sl87i/04-reminder-list.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/ytjk18v6bik593i/07-reminder-list2.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/3vodu8aublrsp0q/06-reminder-item1.png" width="250">
<img src="https://dl.dropboxusercontent.com/s/pncaowgr9ik2bcs/06-reminder-item2.png" width="250">


###Трек передвижений
Список время-координаты. Запись трека включается и отключается пользователем. Возможно только при включенном (актуальном) путешествии - трек привязан к путешествию.

<img src="https://dl.dropboxusercontent.com/s/n88zcwd9c2l3st9/08-map.png" width="250">

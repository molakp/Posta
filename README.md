# INTRODUZIONE # 

Le classi usate in questo progetto sono:

1. Email
1. Posta
1. FXMLDocumentController
1. SocketServer
1. ConnectionHandler

## Convenzioni ##
* Per separare i destinatari della email usare un **;**

* Nel file di testo il carattere **|**  separa i vari campi della email

* Una email per riga


## Funzionamento ##
Si avvia l' rmiregistry, si avvia il SocketServer e si avvia Posta tante volte quanti sono i client che si desidera attivare.
Appare una finestra di login, ogni utente è identificato dalla sua email, e la  email è anche il nome del file di testo associato. 
Il programma caricherà le email già presenti nel file di testo.
Utenti presenti:
* silvestro@prog.com
* mario@ciao

All'invio di una mail, il client trasmette al server un oggetto Email. Il server provvede a scrivere sui file/database dei destinatari la nuova email e manda un messaggio int=1 sullo stream ai destinatari per avvisarli della nuova email. I destinatari aggiornano la vista e possono leggere l'email.




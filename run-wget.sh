#! /bin/sh
wget -nH --directory-prefix=web -r -E -k --follow-tags=a,img,link,script http://localhost/

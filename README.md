# Badge Service DX

Lightweight service aimed to generate statics badges for Github **Open source repos**.
**Important**: Badges generated are immutable, if you want to change the values you should re-send creation request again.


## Core

Badge DX use as core [https://badgen.net/](https://badgen.net/) 

## CI Flow
The service helps to you at next scenario:
- You are at Github Actions context
- Your action generate some metric such as mutation test or coverage using your favorite tool. 
- Then you should capture the metric and send a request to this service like this
```bash
curl -X POST "https://badge-dx.herokuapp.com/create" \ 
	-H  "accept: */*" -H  "Content-Type: application/json" \
	-d '{
	   "project":"YOUR_USERNAME_AT_GITHUB",
	   "repo":"YOUR_REPO_NAME_AT_GITHUB",
	   "subject":"mutation-test",
	   "status": "YOUR_METRIC",
	   "color":"COLOR_LIKE_GREEN",
	   "icon":"SOME_ICON_LIKE_GITHUB",
	   "badgeName":"MY_BADGE_NAME"
	}'
```
Response
```json
{
	  "badgeName": "MY_BADGE_NAME",
	  "badgenUrl": "https://badge-dx.herokuapp.com/YOUR_USERNAME_AT_GITHUB/YOUR_REPO_NAME_AT_GITHUB/MY_BADGE_NAME"
}
```
Now you can copy the **badgenUrl** to your README.md

## Hint
Github uses Camo as image proxy server so maybe your badge will cached for a long time. To hack this you can erase Camo cache using:
```bash
curl -X GET "https://badge-dx.herokuapp.com/camo/YOUR_USERNAME_AT_GITHUB/YOUR_REPO_NAME_AT_GITHUB" -H  "accept: */*"
```

## Test with Swagger
[https://badge-dx.herokuapp.com/swagger-ui](https://badge-dx.herokuapp.com/swagger-ui)

## Author
Gonzalo H. Mendoza
[Checkout my repo at Github](https://github.com/yogonza524) or [my personal resume](https://gmendoza.me)

## Issues?
Please write me [to my alter ego email](mailto:yogonza524)
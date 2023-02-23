##How to Guide
Build image:

```shell
> cd docker
> docker build . -f postgres-11.6.Dockerfile -t stw/signposting --no-cache
```

Run image:
```shell
> docker run -it stw/signposting:latest
```

Shell session into container:
```shell
> docker exec -it <container-name> /bin/bash
```

psql command to connect to database:
```shell
> psql signposting -U signposting_user
```

Refer to [psql commands](https://www.postgresql.org/docs/9.2/app-psql.html) for further usage info. 

SELECT EXISTS (SELECT * FROM information_schema.tables WHERE table_schema = 'public' AND table_catalog='signposting');

if [[ `psql -d signposting -c "SELECT EXISTS (SELECT * FROM information_schema.tables WHERE table_schema = 'public' AND table_catalog='signposting');" -U postgres` = "t" ]]; then
echo -e "schema created"
else
echo -e "schema not created"
fi

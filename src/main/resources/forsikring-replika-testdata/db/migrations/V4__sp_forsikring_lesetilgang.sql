DO
$$
    BEGIN
        IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'sp-forsikring')
        THEN
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO "sp-forsikring";
            ALTER DEFAULT PRIVILEGES FOR USER "forsikring-replika-testdata" IN SCHEMA public GRANT SELECT ON TABLES TO "sp-forsikring";
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS public.currencies (
    id bigint NOT NULL DEFAULT 'nextval('currencies_id_seq'::regclass)',
    code character varying(3) COLLATE pg_catalog."default" NOT NULL,
    full_name character varying COLLATE pg_catalog."default" NOT NULL,
    sign character varying(5) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT currencies_pkey PRIMARY KEY (id),
    CONSTRAINT currencies_code_key UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS public.exchange_rates (
    id bigint NOT NULL DEFAULT 'nextval('exchange_rates_id_seq'::regclass)',
    base_currency_id bigint NOT NULL,
    target_currency_id bigint NOT NULL,
    rate double precision NOT NULL,
    CONSTRAINT exchange_rates_pkey PRIMARY KEY (id),
    CONSTRAINT exchange_rates_base_currency_id_target_currency_id_key UNIQUE (base_currency_id, target_currency_id),
    CONSTRAINT exchange_rates_base_currency_id_fkey FOREIGN KEY (base_currency_id)
        REFERENCES public.currencies (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT exchange_rates_target_currency_id_fkey FOREIGN KEY (target_currency_id)
        REFERENCES public.currencies (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);
set -o pipefail

tmp_dir=/tmp/istub-cli
authdataFile=$tmp_dir/authdata
tokens=$tmp_dir/tokens
STUB_BASE_URL=https://inntektstub.nais.preprod.local
STUB_API=${STUB_BASE_URL}/api/v1

function checkConnectivity {
    if ! curl -Is ${STUB_BASE_URL} > /dev/null; then
        msg Tilgang til ${STUB_BASE_URL} ser ikke ut til å være på plass
        return 1
    fi
}

function auth {
    mkdir -p $tmp_dir
    rm $tokens
    touch $tokens
    echo "Brukerident (feks Z123456)":
    read username
    echo Passord:
    read -s password
    echo USER_CREDS=$(echo -n $username:$password | base64) >> $tokens

    curl -s -D $authdataFile "${STUB_API}/user" -H "Authorization: Basic $(basic_auth)" | jq -e '.token' > /dev/null
    if [[ $? -ne 0 ]]; then
        checkConnectivity && msg Fikk feil ved auth mot iStubben, prøv på nytt
        return 1
    fi

    echo JSESSIONID=$(getCookieValue JSESSION) >> $tokens
    echo BIGIP=$(getCookieValue BIGip) >> $tokens
    echo XSRF=$(getCookieValue XSRF) >> $tokens

    # Må av ukjent grunn hente et nytt xsrf-token..
    hent_inntekter 12345699999 > /dev/null # trenger ikke være et eksisterende FNR
    oppdater_xsrf_tokens

    msg Autentisert mot iStubben
}

function hent_inntekter {
    if [[ $# < 1 ]]; then msg "Fødselsnummer må oppgis" && return 1; fi
    local -r fnr=$1

    local -r response=$(
        source $tokens
        curl -s -D $tmp_dir/inntekter-headers "${STUB_API}/person/$fnr/inntekter" \
            -H "X-XSRF-TOKEN: $XSRF" \
            -H "Cookie: JSESSIONID=$JSESSIONID; \
                XSRF-TOKEN=$XSRF; \
                BIGipServer~AutoProv~pool_pp_inntektstub_q1_https_auto=$BIGIP"
    )
    oppdater_xsrf_tokens
    echo $response
}

function oppdater_xsrf_tokens {
    nytt_xsrf=$(getCookieValue XSRF $tmp_dir/inntekter-headers)
    [[ -n "$nytt_xsrf" ]] && sed -i "s/XSRF.*/XSRF=$nytt_xsrf/" $tokens
}

function opprett_inntekter {
    if [[ $# < 2 ]]; then msg "Fødselsnummer og månedslønn må oppgis (orgnr er valgfritt)" && return 1; fi
    local -r fnr=$1
    local -r mndlnn=$2
    local -r orgnr=${3:-805824354}

    local -r months=(januar februar mars april mai juni juli august september oktober november desember)

    for m in {11..0}; do
        _opprett_inntekt ${fnr} ${mndlnn} ${months[$m]} 2022 ${orgnr}
    done

    for m in {11..0}; do
        _opprett_inntekt ${fnr} ${mndlnn} ${months[$m]} 2021 ${orgnr}
    done

    for m in {11..0}; do
        _opprett_inntekt ${fnr} ${mndlnn} ${months[$m]} 2020 ${orgnr}
    done
}

function _opprett_inntekt {
    local -r fnr=$1
    local -r mndlnn=$2
    local -r maaned=$3
    local -r aar=$4
    local -r orgnr=$5

    (
        source $tokens
        curl -s -X PUT "${STUB_API}/person/${fnr}/inntekt" \
            -d '{"id":null,"beloep":'${mndlnn}',"aar":"'${aar}'","maaned":"'${maaned}'","inntektsinformasjonsType":"INNTEKT","inntektstype":"Loennsinntekt","virksomhet":"'${orgnr}'","fordel":"kontantytelse","beskrivelse":"fastloenn","skatteOgAvgiftsregel":null,"inngaarIGrunnlagForTrekk":true,"utloeserArbeidsgiveravgift":true,"startOpptjeningsperiode":null,"sluttOpptjeningsperiode":null,"bruddPaaForretningsregler":null,"skattemessigBosattland":null,"opptjeningsland":null,"tilleggsinformasjon":null,"simulertBrudd":null,"isEditing":true}' \
            -H 'Content-Type: application/json;charset=UTF-8' \
            -H "X-XSRF-TOKEN: $XSRF" \
            -H "Cookie: JSESSIONID=$JSESSIONID; \
                XSRF-TOKEN=$XSRF; \
                BIGipServer~AutoProv~pool_pp_inntektstub_q1_https_auto=$BIGIP"
        echo -e "\n"
    )
}

function slett_alle_inntekter {
    if [[ $# != 1 ]]; then msg "Fødselsnummer må oppgis" && return 1; fi
    local -r fnr=$1
    local -r inntektsdata=$(hent_inntekter $fnr)
    if echo "$inntektsdata" | jq -e .error? ; then msg Feil ved hent av inntekter; return 1; fi
    
    while read -r id; do _slett_inntekt $fnr $id; done < <( echo "$inntektsdata" | jq .[].id )
}

function _slett_inntekt {
    local -r fnr=$1
    local -r SLETT_ID=$2
    
    (
        source $tokens
        curl -s "${STUB_API}/person/${fnr}/inntekter/delete" \
            -H "Authorization: Basic $(basic_auth)" \
            -H 'Accept: application/json' \
            -H "X-XSRF-TOKEN: ${XSRF}" \
            -H 'Content-Type: application/json;charset=UTF-8' \
            -H "Cookie: JSESSIONID=${JSESSIONID}; \
                XSRF-TOKEN=${XSRF}; \
                BIGipServer~AutoProv~pool_pp_inntektstub_q1_https_auto=${BIGIP}" \
            --data-binary '[{"id":'${SLETT_ID}',"inntektsinformasjonsType":"INNTEKT"}]'
        echo
    )
}

function basic_auth {
    grep USER_CREDS $tokens | cut -d= -f2-
}

function getCookieValue {
    local -r filename=${2:-$authdataFile}
    echo $(awk -F'=' '/'${1}'/ {print $2}' ${filename} | cut -d';' -f1)
}

function msg {
    echo -e "\n$@\n"
}

checkConnectivity

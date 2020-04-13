<script>
    import { flip } from 'svelte/animate';
    import { uuid } from '../../scripts/uuid';
    import { getInntekt } from '../../io/http';
    import Card from '../../components/Card.svelte';
    import Input from '../../components/form/Input.svelte';
    import NumberInput from '../../components/form/NumberInput.svelte';
    import AddButton from '../../components/form/AddButton.svelte';
    import Vedtaksperiode from './Vedtaksperiode.svelte';
    import InfoCard from '../../components/InfoCard.svelte';

    let fnr;
    let inntekt;
    let orgnummer;
    let skalSendeInntektsmelding;

    let vedtaksperioder = [{ id: uuid() }];

    const hentInntekt = async event => {
        event.preventDefault();
        const respons = await getInntekt({ fnr });
        respons.json().then(data => (inntekt = data.beregnetMånedsinntekt));
    };

    const leggTilVedtaksperiode = () => {
        vedtaksperioder = [
            ...vedtaksperioder.map(periode => ({ ...periode, isOpen: false })),
            {
                id: uuid(),
                isOpen: true,
                sykdomFom: '2020-01-01',
                sykdomTom: '2020-01-31'
            }
        ];
    };

    const fjernVedtaksperiode = index => {
        vedtaksperioder = [...vedtaksperioder.slice(0, index), ...vedtaksperioder.slice(index + 1)];
    };

    $: console.log(vedtaksperioder);
</script>

<div class="opprett-person">
    <InfoCard>Foreløpig støttes kun én arbeidsgiver pr. person.</InfoCard>
    <Card title="Person">
        <Input bind:value="{fnr}" onBlur="{hentInntekt}" placeholder="Fødselsnummer" required />
        <Input bind:value="{orgnummer}" placeholder="Organisasjonsnummer" required />
        <NumberInput
            bind:value="{inntekt}"
            placeholder="Inntekt"
            required="{skalSendeInntektsmelding}"
            disabled="{!skalSendeInntektsmelding}"
        />
    </Card>

    <AddButton label="Legg til vedtaksperiode/dokument" onClick="{leggTilVedtaksperiode}" />
    <div class="perioder">
        {#each vedtaksperioder as periode, i (periode.id)}
            <div animate:flip="{{ duration: 300 }}">
                <Vedtaksperiode
                    bind:isOpen="{periode.isOpen}"
                    bind:sykdomFom="{periode.sykdomFom}"
                    bind:sykdomTom="{periode.sykdomTom}"
                    onRemove="{() => fjernVedtaksperiode(i)}"
                />
            </div>
        {/each}
    </div>
</div>

<style>
    .flex {
        display: flex;
        align-items: center;
    }
    .column {
        position: relative;
    }
    .opprett-person {
        position: relative;
        display: flex;
        flex-direction: column;
    }
    .edit-menu {
        position: fixed;
        width: 10rem;
        height: 100vh;
        right: 0;
        top: 0;
        background: #fff;
    }
    .perioder {
        padding-left: 3rem;
    }
</style>

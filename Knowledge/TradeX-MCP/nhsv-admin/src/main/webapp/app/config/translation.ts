import { TranslatorContext, Storage } from 'react-jhipster';


TranslatorContext.setDefaultLocale('en');
TranslatorContext.setRenderInnerTextForMissingKeys(false);

export const languages: any = {
  en: { name: 'English' },
  // ko: { name: '한국어' },
  vi: { name: 'Tiếng Việt' },
  // jhipster-needle-i18n-language-key-pipe - JHipster will add/remove languages in this object
};

export const locales = Object.keys(languages).sort();
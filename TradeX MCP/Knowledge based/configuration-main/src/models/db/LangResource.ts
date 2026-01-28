import { Entity, PrimaryGeneratedColumn, Column, OneToMany } from "typeorm";
import { TradexModelsConfiguration } from "tradex-models-ts";
import LangNamespace from "./LangNamespace";
import LangResourceVersion from "./LangResourceVersion";
import LangKey from "./LangKey";

@Entity("t_lang_resource")
export default class LangResource {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "ms_name" })
  public msName: string;

  @OneToMany(
    (objType: any) => LangNamespace,
    (langNamespace: LangNamespace) => langNamespace.langResource,
  )
  public langNamespaces: LangNamespace[];

  @OneToMany(
    (objType: any) => LangResourceVersion,
    (langResourceVersion: LangResourceVersion) =>
      langResourceVersion.langResource,
  )
  public langResourceVersions: LangResourceVersion[];
}

export function parseToLocaleNamespaces(
  langKeys: LangResource[],
): TradexModelsConfiguration.QueryLocaleResponse {
  if (langKeys != null) {
    return langKeys.map((langKey: LangResource) => {
      const data = {
        id: langKey.id,
        msName: langKey.msName,
        namespaces: [],
      };
      if (langKey.langNamespaces != null && langKey.langNamespaces.length > 0) {
        for (let index = 0; index < langKey.langNamespaces.length; index++) {
          data.namespaces.push({
            id: langKey.langNamespaces[index].id,
            namespace: langKey.langNamespaces[index].namespace,
          });
        }
      }
      return data;
    });
  }

  return null;
}

export function parseToLocaleResponse(
  langKey: LangKey,
): TradexModelsConfiguration.LocaleKey {
  if (langKey != null) {
    const response: TradexModelsConfiguration.LocaleKey = {
      id: langKey.id,
      key: langKey.key,
    };
    if (langKey.langTranslates != null && langKey.langTranslates.length > 0) {
      for (let index = 0; index < langKey.langTranslates.length; index++) {
        response[langKey.langTranslates[index].lang] =
          langKey.langTranslates[index].value;
      }
    }
    return response;
  }

  return null;
}

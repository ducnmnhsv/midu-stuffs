import { Models } from "tradex-common";

export default interface IInternalLeaderboardSettingRequest extends Models.IDataRequest{
    partnerId: string;
    optBoard: boolean;
    subAccount?: string;
}
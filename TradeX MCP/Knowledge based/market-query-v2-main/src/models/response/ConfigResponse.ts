import { SUPPORTED_RESOLUTION } from '../../constants';

export default class ConfigResponse {
  public supports_search: boolean = true;
  public supports_group_request: boolean = false;
  public supports_marks: boolean = true;
  public supports_timescale_marks: boolean = false;
  public supportsTime: boolean = true;
  public exchanges: any = [
    { value: '', name: 'All Exchanges', desc: '' },
    { value: 'HOSE', name: 'HOSE', desc: 'HOSE' },
    { value: 'UPCOM', name: 'UPCOM', desc: 'UPCOM' },
    { value: 'HNX', name: 'HNX', desc: 'HNX' },
  ];
  public symbols_types: any = [
    {
      name: 'All types',
      value: '',
    },
    {
      name: 'Stock',
      value: 'stock',
    },
    {
      name: 'Index',
      value: 'index',
    },
    {
      name: 'Futures',
      value: 'futures',
    },
    {
      name: 'CW',
      value: 'cw',
    },
  ];
  public supported_resolutions: any = SUPPORTED_RESOLUTION;
}

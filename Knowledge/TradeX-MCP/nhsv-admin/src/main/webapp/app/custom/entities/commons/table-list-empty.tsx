import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTriangleExclamation } from '@fortawesome/free-solid-svg-icons';
import React from 'react';

export const TableListEmptyComponent = ({ title }) => {
  return (
    <tr>
      <td colSpan={5} className="text-center text-black-50">
        <FontAwesomeIcon icon={faTriangleExclamation} className="p-icon-l" /> {title} info is empty
      </td>
    </tr>
  );
};

export default TableListEmptyComponent;

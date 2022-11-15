import React, {useEffect, useState} from 'react';
import ProCard from '@ant-design/pro-card';
import RcResizeObserver from 'rc-resize-observer';
import type {ProColumns} from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import styles from './split.less';
import {queryRdbDatabase, queryRdbTable} from '@/services/a-cdc/api';
import {useModel} from 'umi';
import { Input } from 'antd';

const { Search } = Input;

const DatabaseList: React.FC = () => {
	const databaseColumns: ProColumns<API.DatabaseListItem>[] = [
		{
			title: '数据库名',
			dataIndex: 'name',
		}
	];

	const {applyInfoModel, setApplyInfoModel} = useModel('ConnectorApplyModel')
	// 选中
	const [selectId, setSelectId] = useState<number>();
	// 模糊查询
	const [queryDatabaseName, setQueryDatabaseName] = useState<string>();

	const onSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const { value } = e.target;
		setQueryDatabaseName(value);
	};
	const onSearch = (value: string) => {
		setQueryDatabaseName(value);
	};

	useEffect(()=>{
		if(applyInfoModel.sinkDatabaseId && applyInfoModel.sinkDatabaseName) {
			if(applyInfoModel.sinkSearchDatabase) {
				setQueryDatabaseName(applyInfoModel.sinkSearchDatabase);
			}
			setSelectId(applyInfoModel.sinkDatabaseId);
		}
	},[applyInfoModel]);

	return (
		<ProTable<API.DatabaseListItem, API.DatabaseQuery>
			onRow={(record) => {
				return {
					onClick: () => {
						setSelectId(record.id);
						setApplyInfoModel({
							...applyInfoModel,
							sinkDatabaseId: record.id,
							sinkDatabaseName: record.name,
							sinkSearchDatabase: queryDatabaseName
						})
					},
				};
			}}

			rowClassName={(record) => {
				if (record.id === selectId && applyInfoModel!.sinkDatabaseId! === selectId) {
					return styles['split-row-select-active']
				}
				return ''

			}}
			params={{
				clusterId: applyInfoModel.sinkClusterId,
				name: queryDatabaseName,
			}}
			request={queryRdbDatabase}
			columns={databaseColumns}
			toolbar={{
				search: <Search defaultValue={queryDatabaseName} value={queryDatabaseName} onChange={onSearchChange} onSearch={onSearch}/>
			}}

			options={false}
			rowKey={(record) => String(record.id)}
			search={false}
			pagination={{
				showSizeChanger: false,
				pageSize: 10
			}}
		/>
	)
};


const TableList: React.FC = () => {
	const tableColumns: ProColumns<API.TableListItem>[] = [
		{
			title: '数据表名',
			dataIndex: 'name',
		}
	];

	const {applyInfoModel, setApplyInfoModel} = useModel('ConnectorApplyModel')
	// 选中
	const [selectId, setSelectId] = useState<number>();
	// 模糊查询
	const [queryTableName, setQueryTableName] = useState<string>();

	const onSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const { value } = e.target;
		setQueryTableName(value);
	};
	const onSearch = (value: string) => {
		setQueryTableName(value);
	};

	useEffect(()=>{
		if(applyInfoModel.sinkDataSetId && applyInfoModel.sinkDataSetName) {
			if(applyInfoModel.sinkSearchDataSet) {
				setQueryTableName(applyInfoModel.sinkSearchDataSet);
			}
			setSelectId(applyInfoModel.sinkDataSetId);
		}
	},[applyInfoModel]);

	return (
		<ProTable<API.TableListItem, API.TableQuery>
			onRow={(record) => {
				return {
					onClick: () => {
						setSelectId(record.id);
						setApplyInfoModel({
							...applyInfoModel,
							sinkDataSetId: record.id,
							sinkDataSetName: record.name,
							sinkSearchDataSet: queryTableName
						})
					},
				};
			}}
			rowClassName={(record) => {
				if (record.id === selectId && applyInfoModel!.sinkDataSetId! === selectId) {
					return styles['split-row-select-active']
				}
				return ''
			}}
			params={{
				databaseId: applyInfoModel.sinkDatabaseId,
				name: queryTableName
			}}
			request={queryRdbTable}
			columns={tableColumns}
			toolbar={{
				search: <Search defaultValue={queryTableName} value={queryTableName} onChange={onSearchChange} onSearch={onSearch}/>
			}}
			options={false}
			rowKey={(record) => String(record.id)}
			search={false}
			pagination={{
				showSizeChanger: false,
				pageSize: 10
			}}
		/>
	)
};

// 主页面
const MainPage: React.FC = () => {
	const [responsive, setResponsive] = useState(false);
	return (
		<ProCard
			title="选择数据库"
			bordered
			hoverable
			headerBordered
			style={{
				marginBottom: 16,
				minWidth: 800,
				maxWidth: '100%',
			}}>

			<RcResizeObserver
				key="resize-observer"
				onResize={(offset) => {
					setResponsive(offset.width < 100);
				}}
			>
				<ProCard bordered split={responsive ? 'horizontal' : 'vertical'} >
					<ProCard colSpan="50%">
						<DatabaseList />
					</ProCard>
					<ProCard colSpan="50%" >
						<TableList />
					</ProCard>
				</ProCard>
			</RcResizeObserver>
		</ProCard>
	)
};

export default MainPage;

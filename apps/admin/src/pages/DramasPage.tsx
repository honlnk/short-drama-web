import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { App, Button, Image, Input, Popconfirm, Space, Table, Tag } from 'antd'
import {
  deleteDrama,
  getAdminDrama,
  listDramas,
  updateDrama,
  type AdminDramaRow,
} from '../api/admin'
import { getApiErrorMessage } from '../api/client'

/** 短剧管理（默认页）：分页表格 + 剧名检索 + 新增入口 + 行操作（编辑/剧集/上下架/删除二次确认）。 */
export default function DramasPage() {
  const { message } = App.useApp()
  const navigate = useNavigate()
  const [rows, setRows] = useState<AdminDramaRow[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0) // 后端 0 起
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)
  const [acting, setActing] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const result = await listDramas(page, 10, keyword || undefined)
      setRows(result.content)
      setTotal(result.total)
    } catch (err) {
      message.error(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [page, keyword, message])

  useEffect(() => {
    void load()
  }, [load])

  const toggleStatus = async (row: AdminDramaRow) => {
    setActing(true)
    try {
      const detail = await getAdminDrama(row.id)
      const next = row.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE'
      await updateDrama(row.id, {
        title: detail.title,
        description: detail.description,
        category: detail.category,
        coverUrl: detail.coverUrl,
        status: next,
      })
      message.success(next === 'ONLINE' ? '已上架' : '已下架')
      await load()
    } catch (err) {
      message.error(getApiErrorMessage(err))
    } finally {
      setActing(false)
    }
  }

  const remove = async (row: AdminDramaRow) => {
    try {
      await deleteDrama(row.id)
      message.success(`已删除《${row.title}》`)
      // 删掉当前页最后一条时回退一页，避免停在空页
      if (rows.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        await load()
      }
    } catch (err) {
      message.error(getApiErrorMessage(err))
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap style={{ justifyContent: 'space-between', width: '100%' }}>
        <Input.Search
          placeholder="按剧名检索"
          allowClear
          defaultValue={keyword}
          onSearch={(value) => {
            setPage(0)
            setKeyword(value)
          }}
          style={{ width: 260 }}
        />
        <Button type="primary" onClick={() => navigate('/dramas/new')}>
          新增短剧
        </Button>
      </Space>

      <Table<AdminDramaRow>
        rowKey="id"
        loading={loading}
        dataSource={rows}
        pagination={{
          current: page + 1,
          pageSize: 10,
          total,
          showTotal: (t) => `共 ${t} 部`,
          onChange: (p) => setPage(p - 1),
        }}
        columns={[
          {
            title: '封面',
            dataIndex: 'coverUrl',
            width: 72,
            render: (url: string) => (
              <Image src={url} width={44} height={62} style={{ objectFit: 'cover', borderRadius: 4 }} preview={false} />
            ),
          },
          { title: '剧名', dataIndex: 'title', ellipsis: true },
          { title: '分类', dataIndex: 'category', width: 90 },
          { title: '集数', dataIndex: 'episodeCount', width: 70, align: 'center' },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (status: string) =>
              status === 'ONLINE' ? <Tag color="green">上架</Tag> : <Tag color="red">下架</Tag>,
          },
          { title: '点赞数', dataIndex: 'likeCount', width: 90, align: 'right' },
          {
            title: '创建时间',
            dataIndex: 'createdAt',
            width: 150,
            render: (t: string) => (t ? t.slice(0, 16).replace('T', ' ') : '-'),
          },
          {
            title: '操作',
            key: 'actions',
            width: 260,
            render: (_, row) => (
              <Space size="small" wrap>
                <Link to={`/dramas/${row.id}/edit`}>编辑</Link>
                <Link to={`/dramas/${row.id}/episodes`}>剧集管理</Link>
                <Button type="link" size="small" disabled={acting} onClick={() => void toggleStatus(row)}>
                  {row.status === 'ONLINE' ? '下架' : '上架'}
                </Button>
                <Popconfirm
                  title="确认删除这部短剧？"
                  description="将同时删除其剧集、点赞、收藏与观看进度（磁盘文件保留）"
                  okText="删除"
                  okButtonProps={{ danger: true }}
                  cancelText="取消"
                  onConfirm={() => void remove(row)}
                >
                  <Button type="link" size="small" danger>
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
    </Space>
  )
}

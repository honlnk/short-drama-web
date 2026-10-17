import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Space, Table, Upload } from 'antd'
import {
  createEpisode,
  deleteEpisode,
  getAdminDrama,
  listEpisodes,
  updateEpisode,
  uploadFile,
  type EpisodeRow,
} from '../api/admin'
import { getApiErrorMessage } from '../api/client'

interface EpisodeFormValues {
  episodeNo: number
  title: string
  videoUrl: string
  durationSec: number
}

/** 剧集管理：表格 + 新增/编辑 Modal（视频上传后自动读时长填 durationSec，读不到手填）。 */
export default function EpisodesPage() {
  const { id } = useParams()
  const { message } = App.useApp()
  const [dramaTitle, setDramaTitle] = useState('')
  const [rows, setRows] = useState<EpisodeRow[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<EpisodeRow | null>(null)
  const [videoUrl, setVideoUrl] = useState('')
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm<EpisodeFormValues>()

  const load = useCallback(async () => {
    if (!id) {
      return
    }
    setLoading(true)
    try {
      const [detail, episodes] = await Promise.all([getAdminDrama(id), listEpisodes(id)])
      setDramaTitle(detail.title)
      setRows(episodes)
    } catch (err) {
      message.error(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [id, message])

  useEffect(() => {
    void load()
  }, [load])

  const openCreate = () => {
    setEditing(null)
    setVideoUrl('')
    form.resetFields()
    form.setFieldsValue({ episodeNo: rows.length + 1 })
    setModalOpen(true)
  }

  const openEdit = (row: EpisodeRow) => {
    setEditing(row)
    setVideoUrl(row.videoUrl)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  /** 上传成功后用 video 元素读时长自动回填（读不到则管理员手填）。 */
  const probeDuration = (url: string) => {
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.onloadedmetadata = () => {
      if (Number.isFinite(video.duration) && video.duration > 0) {
        form.setFieldValue('durationSec', Math.max(1, Math.round(video.duration)))
      }
    }
    video.src = url
  }

  const handleVideoUpload = async (file: File) => {
    try {
      const url = await uploadFile(file, 'video')
      setVideoUrl(url)
      form.setFieldValue('videoUrl', url)
      probeDuration(url)
      message.success('视频上传成功')
    } catch (err) {
      message.error(getApiErrorMessage(err))
    }
  }

  const onFinish = async (values: EpisodeFormValues) => {
    if (!id) {
      return
    }
    setSaving(true)
    try {
      if (editing) {
        await updateEpisode(editing.id, values)
        message.success('剧集已更新')
      } else {
        await createEpisode(id, values)
        message.success('剧集已添加')
      }
      setModalOpen(false)
      await load()
    } catch (err) {
      message.error(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (row: EpisodeRow) => {
    try {
      await deleteEpisode(row.id)
      message.success(`已删除第 ${row.episodeNo} 集`)
      await load()
    } catch (err) {
      message.error(getApiErrorMessage(err))
    }
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap style={{ justifyContent: 'space-between', width: '100%' }}>
        <span>
          剧集管理：<strong>{dramaTitle || `#${id}`}</strong>
        </span>
        <Space>
          <Button type="primary" onClick={openCreate}>
            新增剧集
          </Button>
          <Link to="/">返回短剧列表</Link>
        </Space>
      </Space>

      <Table<EpisodeRow>
        rowKey="id"
        loading={loading}
        dataSource={rows}
        pagination={false}
        columns={[
          { title: '集数', dataIndex: 'episodeNo', width: 70, align: 'center' },
          { title: '标题', dataIndex: 'title', ellipsis: true },
          { title: '时长', dataIndex: 'durationSec', width: 90, render: (s: number) => `${s}s` },
          {
            title: '视频',
            dataIndex: 'videoUrl',
            ellipsis: true,
            render: (url: string) => (
              <a href={url} target="_blank" rel="noreferrer" title={url}>
                预览
              </a>
            ),
          },
          {
            title: '操作',
            key: 'actions',
            width: 130,
            render: (_, row) => (
              <Space size="small">
                <Button type="link" size="small" onClick={() => openEdit(row)}>
                  编辑
                </Button>
                <Popconfirm
                  title={`确认删除第 ${row.episodeNo} 集？`}
                  description="删集不自动重排集数，剩余集数号由编辑表单维护"
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

      <Modal
        title={editing ? `编辑第 ${editing.episodeNo} 集` : '新增剧集'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <Form<EpisodeFormValues> form={form} layout="vertical" onFinish={(values) => void onFinish(values)}>
          <Form.Item name="episodeNo" label="集数" rules={[{ required: true, message: '请输入集数' }]}>
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="如：第1集 相遇" maxLength={100} />
          </Form.Item>
          <Form.Item label="视频（mp4 ≤50MB）" required>
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Upload accept=".mp4" showUploadList={false} maxCount={1} customRequest={({ file }) => {
                void handleVideoUpload(file as File)
              }}>
                <Button>{videoUrl ? '重新上传视频' : '上传视频'}</Button>
              </Upload>
              {videoUrl && (
                <a href={videoUrl} target="_blank" rel="noreferrer" title={videoUrl}>
                  {videoUrl}
                </a>
              )}
            </Space>
          </Form.Item>
          <Form.Item name="videoUrl" rules={[{ required: true, message: '请先上传视频' }]} style={{ display: 'none' }}>
            <Input />
          </Form.Item>
          <Form.Item
            name="durationSec"
            label="时长（秒）"
            rules={[{ required: true, message: '请输入时长' }]}
            extra="上传视频后自动读取，读不到时手工填写"
          >
            <InputNumber min={1} precision={0} style={{ width: '100%' }} />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saving}>
              {editing ? '保存' : '添加'}
            </Button>
            <Button onClick={() => setModalOpen(false)}>取消</Button>
          </Space>
        </Form>
      </Modal>
    </Space>
  )
}

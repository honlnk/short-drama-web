import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { App, Button, Card, Form, Input, Select, Space, Upload } from 'antd'
import { createDrama, getAdminDrama, updateDrama, uploadFile } from '../api/admin'
import { getApiErrorMessage } from '../api/client'

interface DramaFormValues {
  title: string
  description: string
  category: string
  coverUrl: string
  status: string
}

/** 短剧表单（新增/编辑共用）：封面 Upload 直传后把返回 URL 填入表单（计划任务4 第4条）。 */
export default function DramaFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const { message } = App.useApp()
  const navigate = useNavigate()
  const [form] = Form.useForm<DramaFormValues>()
  const [coverUrl, setCoverUrl] = useState('')
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(isEdit)

  useEffect(() => {
    if (!isEdit) {
      return
    }
    getAdminDrama(id)
      .then((detail) => {
        form.setFieldsValue(detail)
        setCoverUrl(detail.coverUrl)
      })
      .catch((err) => message.error(getApiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [id, isEdit, form, message])

  const handleCoverUpload = async (file: File) => {
    try {
      const url = await uploadFile(file, 'cover')
      setCoverUrl(url)
      form.setFieldValue('coverUrl', url)
      message.success('封面上传成功')
    } catch (err) {
      message.error(getApiErrorMessage(err))
    }
  }

  const onFinish = async (values: DramaFormValues) => {
    setSaving(true)
    try {
      if (isEdit) {
        await updateDrama(id, values)
        message.success('保存成功')
      } else {
        await createDrama(values)
        message.success('新增成功')
      }
      navigate('/')
    } catch (err) {
      message.error(getApiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card title={isEdit ? `编辑短剧 #${id}` : '新增短剧'} loading={loading}>
      <Form<DramaFormValues>
        form={form}
        layout="vertical"
        style={{ maxWidth: 560 }}
        initialValues={{ status: 'ONLINE', description: '' }}
        onFinish={(values) => void onFinish(values)}
      >
        <Form.Item name="title" label="剧名" rules={[{ required: true, message: '请输入剧名' }, { max: 100, message: '不超过 100 字' }]}>
          <Input placeholder="请输入剧名" />
        </Form.Item>
        <Form.Item name="category" label="分类" rules={[{ required: true, message: '请输入分类（如 甜宠/都市/古装）' }]}>
          <Input placeholder="如：甜宠" maxLength={20} />
        </Form.Item>
        <Form.Item name="description" label="简介" rules={[{ max: 2000, message: '简介过长' }]}>
          <Input.TextArea rows={3} placeholder="一句话卖点或剧情简介" />
        </Form.Item>
        <Form.Item label="封面（jpg/png/webp ≤5MB）" required>
          <Space direction="vertical" size="small">
            <Upload
              accept=".jpg,.jpeg,.png,.webp"
              showUploadList={false}
              maxCount={1}
              customRequest={({ file }) => {
                void handleCoverUpload(file as File)
              }}
            >
              <Button>{coverUrl ? '重新上传封面' : '上传封面'}</Button>
            </Upload>
            {coverUrl && (
              <img
                src={coverUrl}
                alt="封面预览"
                style={{ width: 68, height: 96, objectFit: 'cover', borderRadius: 6, border: '1px solid #eee' }}
              />
            )}
          </Space>
        </Form.Item>
        <Form.Item
          name="coverUrl"
          rules={[{ required: true, message: '请先上传封面' }]}
          style={{ display: 'none' }}
        >
          <Input />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true }]}>
          <Select
            options={[
              { value: 'ONLINE', label: '上架（ONLINE）' },
              { value: 'OFFLINE', label: '下架（OFFLINE）' },
            ]}
          />
        </Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={saving}>
            {isEdit ? '保存' : '创建'}
          </Button>
          <Button onClick={() => navigate('/')}>取消</Button>
        </Space>
      </Form>
    </Card>
  )
}

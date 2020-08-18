from __future__ import division, print_function, unicode_literals
import numpy as np
import torch
import torch.utils.data
import torchvision.transforms as transforms
from torch.autograd import Variable
import matplotlib.pyplot as plt
%matplotlib inline
plt.ion()
# Import other modules if required
# Can use other libraries as well
import xml.etree.ElementTree as ET
import torchvision.models as models
import torch.nn as nn
import torch.optim as optim
from glob import glob
import os,sys
from PIL import Image

resnet_input = 225 #size of resnet18 input images



transformations = transforms.Compose([
            transforms.RandomResizedCrop(224),
            transforms.RandomHorizontalFlip(),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])])
train_dataset = voc_dataset(root_dir='Datasets/Train_VOCdevkit/', train=True, transform=transformations) # Supply proper root_dir
train_loader = torch.utils.data.DataLoader(dataset=train_dataset, batch_size=batch_size, shuffle=True)


def sliding_window(width,height):
    box_dim =[[128,128],[200,200],[400,400],[180,360],[90,180],[180,90]]
    fe_size = (800//40)
    ctr_x = np.arange(16, (fe_size+1) * 16, 16)
    ctr_y = np.arange(16, (fe_size+1) * 16, 16)
    ctr = np.zeros((len(ctr_x)*len(ctr_y),2))

    index = 0
    for x in range(len(ctr_x)):
        for y in range(len(ctr_y)):
            ctr[index, 1] = ctr_x[x] - 8
            ctr[index, 0] = ctr_y[y] - 8
            index +=1
    boxes = np.zeros(((fe_size * fe_size * 9), 4))
    index = 0
    for c in ctr:
        ctr_y, ctr_x = c
        for i in range(len(box_dim)):
            h = box_dim[i][0]
            w = box_dim[i][1]
            boxes[index, 0] = ctr_x - w / 2.
            boxes[index, 1] = ctr_y - h / 2.
            boxes[index, 2] = ctr_x + w / 2.
            boxes[index, 3] = ctr_y + h / 2.
            index += 1
    bbox = np.asarray([[20, 30, 400, 500], [300, 400, 500, 600]], dtype=np.float32) # [y1, x1, y2, x2] format
    labels = np.asarray([6, 8], dtype=np.int8) # 0 represents backgrounda
    index_inside = np.where(
            (boxes[:, 0] >= 0) &
            (boxes[:, 1] >= 0) &
            (boxes[:, 2] <= width) &
            (boxes[:, 3] <= height)
        )[0]
    label = np.empty((len(index_inside), ), dtype=np.int32)
    label.fill(-1)
    valid_boxes = boxes[index_inside]
    return valid_boxes



def non_maximum_supression(boxes,threshold = 0.5):
    if len(boxes) == 0:
        return []

    pick = []

    x1 = boxes[:,0]
    y1 = boxes[:,1]
    x2 = boxes[:,2]
    y2 = boxes[:,3]

    area = (x2 - x1 + 1) * (y2 - y1 + 1)
    idxs = np.argsort(y2)

    while len(idxs) > 0:
        last = len(idxs) - 1
        i = idxs[last]
        pick.append(i)
        suppress = [last]

        for pos in range(0, last):
            j = idxs[pos]

            xx1 = max(x1[i], x1[j])
            yy1 = max(y1[i], y1[j])
            xx2 = min(x2[i], x2[j])
            yy2 = min(y2[i], y2[j])

            w = max(0, xx2 - xx1 + 1)
            h = max(0, yy2 - yy1 + 1)

            overlap = float(w * h) / area[j]

            if overlap > threshold:
                suppress.append(pos)

        idxs = np.delete(idxs, suppress)

    return boxes[pick]



trans1 = transforms.ToPILImage()
trans = transforms.ToTensor()

device = torch.device("cuda" if torch.cuda.is_available() 
                                  else "cpu")
model = torch.load('one_layer_model.pth')
criterion = nn.CrossEntropyLoss()
# Update if any errors occur
optimizer = optim.SGD(model.parameters(), learning_rate, hyp_momentum)


Test_dataset = []
for f in data:
    im = Image.open('Datasets/Test_VOCdevkit/VOC2007/JPEGImages/' + f[0]+'.jpg')
    fp = im.fp
    im.load()
    fp.closed
    Test_dataset.append(im)
a = Test_dataset
Test_dataset = []
Test_dataset.append(a[0])
Test_dataset.append(a[1])
Test_dataset.append(a[2])
Test_dataset.append(a[3])



#One Layer Detection
def test(model):
    results = []
    for data in Test_dataset:
        image = data
        w, h = image.size[0], image.size[1]
        boxes = sliding_window(w, h)
        res = []
        for box in boxes:
            area = (box[0], box[1], box[2], box[3])
            im = image.crop(area)
            im = transformations(im)
            im = im.unsqueeze_(0)
            im = im.to(device)
            k  = model.forward(im)
            prob = torch.nn.functional.softmax(k, dim=1)[0]
            cls = prob.data.cpu().numpy().argmax()
            if(cls!=0):
                res.append(box)
#             res.append([cls, prob[cls]])
#         prob_pclss = [0.3,0.3, 0.3, 0.3]
#         bbox_pclss = [-1,-1,-1,-1]
#         for clss in range(4):
#             for i, r in enumerate(res):
#                 if(r[0]==clss and r[1]>prob_pclss[clss]):
#                     prob_pclss[clss] = r[1]
#                     bbox_pclss[clss] = i
        bboxes = non_maximum_supression(np.array(res),0.5)
#         for i in bbox_pclss:
#             if(i!=-1):
#                 bboxes.append(boxes[i])
        results.append(bboxes)
    return results
results = test(model)